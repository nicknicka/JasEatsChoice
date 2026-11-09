"""OpenAI-compatible 假服务，供后端集成测试与 Playwright 在无真实厂商密钥时跑通 AI 主链路。

只依赖标准库：同一个进程既做健康检查探针，也做模型端点。返回什么由**请求里的标记**决定，
控制路径不依赖跨请求共享状态（此前用 mode 文件的写法在并行用例下会互相干扰）；
另有一份只读计数端点，供集成测试断言「上游到底收到了什么」：

Chat Completions（/v1/chat/completions）
  * 消息里带图片（Vision）
      - 图片字节含 FAKE_VISION_UNSUPPORTED  在发送任何事件前返回 HTTP 400「不支持图片」
      - 图片字节含 FAKE_VISION_NOTDISH      返回 isDish=false 的合法 JSON
      - 图片字节含 FAKE_VISION_ERROR        返回 HTTP 500
      - 其它                                返回 isDish=true 的完整识别结果
  * 纯文本（文本对话 / 食谱优化）
      - 文本含 [401]   返回 HTTP 401，模拟上游鉴权失败
      - 文本含 [429]   返回 HTTP 429，模拟上游限流
      - 文本含 [error] 返回 HTTP 500（流式在发送任何事件前返回）
      - 文本含 [stall] 且 stream=true：先发 SSE 响应头再挂起连接，触发后端超时
      - 提示词含「优化目标」：按提示词里的目标码回显 goal，非流式直接返回整段结果 JSON
      - 其它                         返回 "ok"（供连通性探针使用）
  * stream=true 且为食谱优化：逐 token 推送一份合法结果，末尾附结果 JSON 与 [DONE]

Embeddings（/v1/embeddings）
  * input 文本含 [401]/[429] 时返回对应 HTTP 错误，其它返回确定性的向量（维度可指定）

测试自省（供 HTTP 级断言使用）
  * GET  /_introspect  返回累计请求计数与最近一次请求的关键事实
  * POST /_reset       清零计数，便于用例隔离
"""

import base64
import hashlib
import json
import re
import sys
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

DEFAULT_PORT = 4011

# ---- 请求标记 ----
VISION_UNSUPPORTED = "FAKE_VISION_UNSUPPORTED"
VISION_NOTDISH = "FAKE_VISION_NOTDISH"
VISION_ERROR = "FAKE_VISION_ERROR"

GOAL_PATTERN = re.compile(r"[(（](balanced|fat_loss|muscle_gain|blood_sugar_control)[)）]")

# 与服务层 parseRecipeResult 约定的最终结构保持一致，token 流结束前整段推送。
RECIPE_RESULT = {
    "goal": "balanced",
    "recipe": {
        "name": "番茄炒蛋",
        "difficulty": "简单",
        "ingredients": "番茄2个、鸡蛋3个、盐少许",
        "steps": "1. 打蛋 2. 炒蛋 3. 下番茄 4. 调味出锅",
    },
    "nutrition": {
        "original": {"calorie": 220, "protein": 14, "fat": 15, "carb": 8},
        "optimized": {"calorie": 190, "protein": 16, "fat": 11, "carb": 9},
    },
    "improvements": [
        {"title": "减少用油", "detail": "炒制用油从 15g 降到 8g"},
        {"title": "增加蛋白质", "detail": "鸡蛋增加到 3 个，蛋白提升约 2g"},
    ],
    "estimationNote": "营养数值为 AI 估算，仅供参考",
}

# 与服务层 parseDishRecognitionResult 约定的字段保持一致。
DISH_RESULT = {
    "isDish": True,
    "name": "番茄炒蛋",
    "calories": 180,
    "protein": 10,
    "fat": 12,
    "carbs": 8,
    "difficulty": "简单",
    "preparationTime": "15分钟",
    "ingredients": ["番茄", "鸡蛋"],
    "tags": ["家常菜"],
    "confidence": 0.92,
    "nutritionScore": 8,
    "nutritionEstimated": True,
}

NOT_DISH_RESULT = {"isDish": False, "reason": "请上传食物图片"}

TOKEN_CHUNKS = ["下面", "是", "优化", "后的", "食谱："]

# 每个 token 之间留出间隔，让前端有稳定的时间片展示实时输出。
TOKEN_INTERVAL_SECONDS = 0.15

DEFAULT_EMBEDDING_DIMENSIONS = 1536

# 只在进程内存里累计，供 /_introspect 断言使用；测试用 /_reset 清零。
_STATE = {
    "chatRequests": 0,
    "streamRequests": 0,
    "imageRequests": 0,
    "embeddingRequests": 0,
    "lastImageMime": None,
    "lastRecipeGoal": None,
    "lastPromptText": None,
}


def reset_state():
    for key in list(_STATE):
        _STATE[key] = 0 if key.endswith("Requests") else None


def iter_message_parts(payload):
    """逐条产出 message.content 里的片段，content 可能是字符串或 content array。"""
    for message in payload.get("messages") or []:
        if not isinstance(message, dict):
            continue
        content = message.get("content")
        if isinstance(content, str):
            yield {"type": "text", "text": content}
        elif isinstance(content, list):
            for item in content:
                if isinstance(item, dict):
                    yield item


def merge_content_texts(payload):
    for part in iter_message_parts(payload):
        if not isinstance(part, dict):
            continue
        # 兼容 {type:text,text:...}
        text = part.get("text")
        if isinstance(text, str):
            yield text
        # 兼容 LangChain4j 之外的 {type:text,content:...}
        content = part.get("content")
        if isinstance(content, str):
            yield content
        for key in ("input_text", "value"):
            value = part.get(key)
            if isinstance(value, str):
                yield value


def extract_image(part):
    """从 content 片段里取出 (mime, raw_bytes)，不是图片片段时返回 None。"""
    if not isinstance(part, dict):
        return None
    candidates = []
    for key in ("image_url", "image", "data"):
        value = part.get(key)
        if value is not None:
            candidates.append(value)
    for value in candidates:
        url = value.get("url") if isinstance(value, dict) else value
        if not isinstance(url, str):
            continue
        mime, raw = decode_data_url(url)
        if raw is not None:
            return mime, raw
    return None


def decode_data_url(url):
    """解析 data:<mime>;base64,<payload>，也接受纯 base64。"""
    if url.startswith("data:"):
        header, _, payload = url.partition(",")
        mime = header[5:].split(";")[0] or "image/jpeg"
        try:
            return mime, base64.b64decode(payload)
        except (ValueError, base64.binascii.Error):
            return mime, None
    try:
        return "image/jpeg", base64.b64decode(url, validate=False)
    except (ValueError, base64.binascii.Error):
        return None, None


def select_marker(text, marker):
    return marker in text


def chat_mode(payload):
    """返回 (mode, image)。mode ∈ vision/text；错误标记在调用方判断。"""
    image = None
    for part in iter_message_parts(payload):
        found = extract_image(part)
        if found is not None:
            image = found
            break
    return image


def prompt_text(payload):
    return "\n".join(merge_content_texts(payload))


def goal_from_prompt(text):
    """从提示词里抽出优化目标码，证明 goal 真的进了 Prompt。"""
    match = GOAL_PATTERN.search(text or "")
    return match.group(1) if match else None


def recipe_result_for(text):
    result = json.loads(json.dumps(RECIPE_RESULT, ensure_ascii=False))
    goal = goal_from_prompt(text)
    result["goal"] = goal or "balanced"
    return result


def embedding_vector(text, dimensions):
    seed = hashlib.sha256((text or "").encode("utf-8")).digest()
    values = []
    for index in range(dimensions):
        byte = seed[index % len(seed)]
        values.append(round((byte - 128) / 128.0, 6))
    return values


class FakeAiHandler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, fmt, *args):
        sys.stderr.write("[fake-ai] " + (fmt % args) + "\n")
        sys.stderr.flush()

    # ---- 基础响应helpers ----
    def send_json(self, status, payload):
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def send_openai_error(self, status, message, error_type="invalid_request_error", code=None):
        self.send_json(status, {"error": {"message": message, "type": error_type, "code": code}})

    def read_payload(self):
        length = int(self.headers.get("Content-Length") or 0)
        raw = self.rfile.read(length) if length else b""
        try:
            return json.loads(raw or b"{}")
        except json.JSONDecodeError:
            return {}

    # ---- 路由 ----
    def do_GET(self):
        if self.path.startswith("/v1/models"):
            self.send_json(200, {"object": "list", "data": [{"id": "fake-model", "object": "model"}]})
            return
        if self.path.startswith("/_introspect"):
            self.send_json(200, dict(_STATE))
            return
        self.send_json(404, {"error": {"message": "not found"}})

    def do_POST(self):
        if self.path.startswith("/_reset"):
            reset_state()
            self.send_json(200, {"ok": True})
            return
        if self.path.startswith("/v1/embeddings"):
            self.handle_embeddings()
            return
        if self.path.startswith("/v1/chat/completions"):
            self.handle_chat_completions()
            return
        self.send_json(404, {"error": {"message": "not found"}})

    # ---- Chat Completions ----
    def handle_chat_completions(self):
        payload = self.read_payload()
        stream = bool(payload.get("stream"))
        _STATE["chatRequests"] += 1
        if stream:
            _STATE["streamRequests"] += 1

        image = chat_mode(payload)
        if image is not None:
            self.handle_vision(image)
            return

        text = prompt_text(payload)
        _STATE["lastPromptText"] = text[:500]

        if select_marker(text, "[401]"):
            self.send_openai_error(401, "Incorrect API key provided", "authentication_error")
            return
        if select_marker(text, "[429]"):
            self.send_openai_error(429, "Rate limit reached", "rate_limit_error")
            return

        is_recipe = "优化目标" in text
        self.log_message("chat/completions stream=%s recipe=%s", stream, is_recipe)

        if not is_recipe:
            # 连通性探针：只要 200 且非空即可。
            self.send_json(200, self.chat_completion("ok"))
            return

        goal = goal_from_prompt(text)
        _STATE["lastRecipeGoal"] = goal or "balanced"
        result = recipe_result_for(text)

        if not stream:
            self.send_json(200, self.chat_completion(json.dumps(result, ensure_ascii=False)))
            return

        if select_marker(text, "[error]"):
            self.send_openai_error(500, "upstream boom", "server_error")
            return

        if select_marker(text, "[stall]"):
            self.begin_sse()
            # 持续占用连接，让后端 watchdog 触发 AI_TIMEOUT 而不是正常收尾。
            for _ in range(600):
                time.sleep(0.5)
            return

        self.begin_sse()
        self.write_chunk({"role": "assistant"})
        for piece in TOKEN_CHUNKS:
            self.write_chunk({"content": piece})
            time.sleep(TOKEN_INTERVAL_SECONDS)
        self.write_chunk({"content": json.dumps(result, ensure_ascii=False)})
        time.sleep(TOKEN_INTERVAL_SECONDS)
        self.write_chunk({}, finish_reason="stop")
        self.wfile.write(b"data: [DONE]\n\n")
        self.wfile.flush()

    def handle_vision(self, image):
        mime, raw = image
        _STATE["imageRequests"] += 1
        _STATE["lastImageMime"] = mime
        text_bytes = raw or b""
        self.log_message("vision mime=%s bytes=%d", mime, len(text_bytes))

        if VISION_UNSUPPORTED.encode() in text_bytes:
            self.send_openai_error(
                400, "This model does not support image input.", "invalid_request_error", "invalid_image")
            return
        if VISION_ERROR.encode() in text_bytes:
            self.send_openai_error(500, "vision upstream boom", "server_error")
            return
        if VISION_NOTDISH.encode() in text_bytes:
            self.send_json(200, self.chat_completion(json.dumps(NOT_DISH_RESULT, ensure_ascii=False)))
            return
        self.send_json(200, self.chat_completion(json.dumps(DISH_RESULT, ensure_ascii=False)))

    def chat_completion(self, content):
        return {
            "id": "chatcmpl-fake",
            "object": "chat.completion",
            "created": int(time.time()),
            "model": "fake-model",
            "choices": [{
                "index": 0,
                "message": {"role": "assistant", "content": content},
                "finish_reason": "stop",
            }],
            "usage": {"prompt_tokens": 5, "completion_tokens": 1, "total_tokens": 6},
        }

    # ---- Embeddings ----
    def handle_embeddings(self):
        payload = self.read_payload()
        _STATE["embeddingRequests"] += 1

        raw_input = payload.get("input")
        if isinstance(raw_input, list):
            texts = [str(item) for item in raw_input]
        elif raw_input is None:
            texts = [""]
        else:
            texts = [str(raw_input)]
        joined = "\n".join(texts)

        if select_marker(joined, "[401]"):
            self.send_openai_error(401, "Incorrect API key provided", "authentication_error")
            return
        if select_marker(joined, "[429]"):
            self.send_openai_error(429, "Rate limit reached", "rate_limit_error")
            return
        if select_marker(joined, "[error]"):
            self.send_openai_error(500, "embedding upstream boom", "server_error")
            return

        dimensions = payload.get("dimensions")
        if not isinstance(dimensions, int) or dimensions <= 0:
            dimensions = DEFAULT_EMBEDDING_DIMENSIONS
        self.log_message("embeddings count=%d dimensions=%d", len(texts), dimensions)

        data = [{
            "object": "embedding",
            "index": index,
            "embedding": embedding_vector(text, dimensions),
        } for index, text in enumerate(texts)]
        self.send_json(200, {
            "object": "list",
            "data": data,
            "model": payload.get("model") or "fake-model",
            "usage": {"prompt_tokens": len(joined), "total_tokens": len(joined)},
        })

    # ---- SSE helpers ----
    def begin_sse(self):
        # 关掉 keep-alive：连接由本次响应独占，避免复用导致的下一个请求读到残留数据。
        self.close_connection = True
        self.send_response(200)
        self.send_header("Content-Type", "text/event-stream")
        self.send_header("Cache-Control", "no-cache")
        self.send_header("Connection", "close")
        self.end_headers()

    def write_chunk(self, delta, finish_reason=None):
        chunk = {
            "id": "chatcmpl-fake",
            "object": "chat.completion.chunk",
            "created": int(time.time()),
            "model": "fake-model",
            "choices": [{"index": 0, "delta": delta, "finish_reason": finish_reason}],
        }
        self.wfile.write(("data: " + json.dumps(chunk, ensure_ascii=False) + "\n\n").encode("utf-8"))
        self.wfile.flush()


def main():
    port = int(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_PORT
    server = ThreadingHTTPServer(("0.0.0.0", port), FakeAiHandler)
    sys.stderr.write("[fake-ai] listening on %d\n" % port)
    sys.stderr.flush()
    server.serve_forever()


if __name__ == "__main__":
    main()
