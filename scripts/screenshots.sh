#!/usr/bin/env bash
# 全量网页截图：按需拉起 Web 前端开发服务器 → 逐页截图 → 重建索引页。
#
# 用法：
#   make screenshots                                     # 全量 75 页 + 重建 index.html/README.md
#   ONLY=05-order-confirmation scripts/screenshots.sh     # 只补跑指定 slug（按 role/slug 合并进 manifest）
#
# 前置：后端容器栈已 `make up` 且健康。Web 前端由本脚本按需拉起，退出时只回收自己起的那个。
# 产出：output/web-screenshots/（在 .gitignore 中，不入库）
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

# nginx 只监听 IPv4，Vite 继承同一约定；用 localhost 可能先解析到 ::1 被拒，故统一走 127.0.0.1
export VITE_DEV_HOST="${VITE_DEV_HOST:-127.0.0.1}"
export VITE_DEV_PORT="${VITE_DEV_PORT:-5173}"
export API_PROXY_TARGET="${API_PROXY_TARGET:-http://127.0.0.1:7777}"
export WS_PROXY_TARGET="${WS_PROXY_TARGET:-http://127.0.0.1:11277}"
export SCREENSHOT_BASE_URL="${SCREENSHOT_BASE_URL:-http://${VITE_DEV_HOST}:${VITE_DEV_PORT}}"
export TEST_API_BASE="${TEST_API_BASE:-${API_PROXY_TARGET}/api}"

VITE_PID=""
cleanup() {
  if [[ -n "$VITE_PID" ]]; then
    kill "$VITE_PID" 2>/dev/null || true
    wait "$VITE_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT

if [[ ! -d apps/client/node_modules || ! -d tests/node_modules ]]; then
  echo "依赖缺失，先执行 make deps" >&2
  exit 1
fi

echo "==> 1/3 检查后端栈"
if ! curl -fsS -o /dev/null "${TEST_API_BASE}/actuator/health" 2>/dev/null; then
  echo "  后端不可达（${TEST_API_BASE}），先执行 make up" >&2
  exit 1
fi
echo "  后端健康"

echo "==> 2/3 准备 Web 前端开发服务器"
if curl -fsS -o /dev/null "${SCREENSHOT_BASE_URL}/" 2>/dev/null; then
  echo "  复用已在运行的 ${SCREENSHOT_BASE_URL}"
else
  if lsof -nP -iTCP:"${VITE_DEV_PORT}" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "  端口 ${VITE_DEV_PORT} 被其他进程占用且不是可用前端，请先处理" >&2
    exit 1
  fi
  VITE_LOG="$(mktemp -t jaseats-screenshots-vite)"
  (cd apps/client && exec ./node_modules/.bin/vite --config vite.web.config.js) >"$VITE_LOG" 2>&1 &
  VITE_PID=$!
  for _ in $(seq 1 60); do
    curl -fsS -o /dev/null "${SCREENSHOT_BASE_URL}/" 2>/dev/null && break
    if ! kill -0 "$VITE_PID" 2>/dev/null; then
      echo "  前端启动失败，vite 日志：" >&2
      tail -30 "$VITE_LOG" >&2
      exit 1
    fi
    sleep 2
  done
  if ! curl -fsS -o /dev/null "${SCREENSHOT_BASE_URL}/" 2>/dev/null; then
    echo "  ${SCREENSHOT_BASE_URL} 未就绪，vite 日志：" >&2
    tail -30 "$VITE_LOG" >&2
    exit 1
  fi
  echo "  已启动 ${SCREENSHOT_BASE_URL}（退出时自动回收）"
fi

echo "==> 3/3 逐页截图 + 重建索引页"
node scripts/capture_screens.js
node scripts/build_screenshot_index.js
echo "产出目录：${SCREENSHOT_OUT:-output/web-screenshots}"
