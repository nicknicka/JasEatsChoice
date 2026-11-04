"""OpenAI-compatible 假服务，供 Playwright 在无真实厂商密钥时跑通 AI 主链路。

只依赖标准库：CI 用同一个 Python 进程既做健康检查探针，也做模型端点。
行为由请求里的标记决定，因此一个进程就能覆盖成功、上游报错、卡死三种场景，
不需要任何跨请求共享状态（此前用 mode 文件的写法在并行用例下会互相干扰）：

  [error]   在发送任何事件前返回 HTTP 500
  [stall]   先发出 SSE 响应头，然后一直挂起连接
  其它情况  [normal] 或未标记，逐 token 推送一份合法的食谱优化结果
"""

import json
import sys
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

DEFAULT_PORT = 4011

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

TOKEN_CHUNKS = ["下面", "是", "优化", "后的", "食谱："]

# 每个 token 之间留出间隔，让前端有稳定的时间片展示实时输出。
TOKEN_INTERVAL_SECONDS = 0.15


def iter_message_texts(payload):
    """取出所有 message.content 的文本片段，content 可能是字符串或 content array。"""
    for message in payload.get("messages") or []:
        if not isinstance(message, dict):
            continue
        content = message.get("content")
        if isinstance(content, str):
            yield content
        elif isinstance(content, list):
            for item in content:
                if isinstance(item, dict) and isinstance(item.get("text"), str):
                    yield item["text"]


def select_mode(payload):
    text = "\n".join(iter_message_texts(payload))
    if "[error]" in text:
        return "error"
    if "[stall]" in text:
        return "stall"
    return "normal"


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
        self.send_json(404, {"error": {"message": "not found"}})

    def do_POST(self):
        if not self.path.startswith("/v1/chat/completions"):
            self.send_json(404, {"error": {"message": "not found"}})
            return

        payload = self.read_payload()
        stream = bool(payload.get("stream"))
        mode = select_mode(payload)
        self.log_message("chat/completions stream=%s mode=%s", stream, mode)

        if not stream:
            self.send_json(200, {
                "id": "chatcmpl-fake",
                "object": "chat.completion",
                "created": int(time.time()),
                "model": "fake-model",
                "choices": [{
                    "index": 0,
                    "message": {"role": "assistant", "content": "ok"},
                    "finish_reason": "stop",
                }],
                "usage": {"prompt_tokens": 5, "completion_tokens": 1, "total_tokens": 6},
            })
            return

        if mode == "error":
            self.send_json(500, {"error": {"message": "upstream boom", "type": "server_error"}})
            return

        if mode == "stall":
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
        self.write_chunk({"content": json.dumps(RECIPE_RESULT, ensure_ascii=False)})
        time.sleep(TOKEN_INTERVAL_SECONDS)
        self.write_chunk({}, finish_reason="stop")
        self.wfile.write(b"data: [DONE]\n\n")
        self.wfile.flush()

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
