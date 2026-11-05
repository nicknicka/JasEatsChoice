#!/usr/bin/env bash
# 一键启动或重启本地开发环境：Docker 栈 + Web 前端开发服务器。
#
# 用法：
#   scripts/start-dev.sh            启动或复用现有环境
#   scripts/start-dev.sh --restart  重启 Docker 服务与前端
#
# Ctrl+C 只停止前端；容器和数据卷保留，需要停栈时执行 make down。
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

VITE_URL="http://localhost:5173"
VITE_PID=""
RESTART=0

case "${1:-}" in
  "")
    ;;
  "--restart")
    RESTART=1
    ;;
  *)
    echo "用法：$0 [--restart]" >&2
    exit 2
    ;;
esac
if [[ $# -gt 1 ]]; then
  echo "用法：$0 [--restart]" >&2
  exit 2
fi

cleanup() {
  if [[ -n "$VITE_PID" ]] && kill -0 "$VITE_PID" 2>/dev/null; then
    kill "$VITE_PID" 2>/dev/null || true
    wait "$VITE_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

stop_frontend() {
  local pids pid command
  pids="$(lsof -tiTCP:5173 -sTCP:LISTEN 2>/dev/null || true)"
  [[ -z "$pids" ]] && return 0

  for pid in $pids; do
    command="$(ps -p "$pid" -o command= 2>/dev/null || true)"
    if [[ "$command" != *"vite.web.config.js"* ]]; then
      echo "端口 5173 被非本项目进程占用：$command" >&2
      return 1
    fi
  done

  echo "==> 停止现有 Web 前端"
  kill $pids 2>/dev/null || true
  for _ in $(seq 1 20); do
    if ! lsof -nP -iTCP:5173 -sTCP:LISTEN >/dev/null 2>&1; then
      return 0
    fi
    sleep 0.25
  done

  echo "现有 Web 前端停止超时。" >&2
  return 1
}

if ! docker info >/dev/null 2>&1; then
  echo "Docker 未运行，请先启动 Docker Desktop。" >&2
  exit 1
fi

if [[ ! -f deploy/.env ]]; then
  echo "==> deploy/.env 不存在，生成本地环境变量"
  make env
fi

if [[ ! -x apps/client/node_modules/.bin/vite ]]; then
  echo "==> 前端依赖不存在，开始安装"
  npm --prefix apps/client install
fi

if [[ $RESTART -eq 1 ]]; then
  stop_frontend
fi

echo "==> 构建并启动 Docker 开发栈"
make up

if [[ $RESTART -eq 1 ]]; then
  echo "==> 重启 Docker 服务"
  docker compose --env-file deploy/.env \
    -f deploy/compose.yml -f deploy/compose.dev.yml restart
  COMPOSE_FLAGS="--env-file deploy/.env -f deploy/compose.yml -f deploy/compose.dev.yml" \
    WAIT_TIMEOUT=300 scripts/wait-healthy.sh mysql redis qdrant spring-boot nginx
fi

if curl -fsS -o /dev/null "$VITE_URL/" 2>/dev/null; then
  echo "==> 前端已在运行：$VITE_URL"
  exit 0
fi

if lsof -nP -iTCP:5173 -sTCP:LISTEN >/dev/null 2>&1; then
  echo "端口 5173 已被其他进程占用，且不是可访问的前端服务。" >&2
  exit 1
fi

echo "==> 启动 Web 前端开发服务器"
(
  cd apps/client
  exec ./node_modules/.bin/vite --config vite.web.config.js
) &
VITE_PID=$!

for _ in $(seq 1 60); do
  if ! kill -0 "$VITE_PID" 2>/dev/null; then
    wait "$VITE_PID" || true
    echo "前端开发服务器启动失败。" >&2
    exit 1
  fi
  if curl -fsS -o /dev/null "$VITE_URL/" 2>/dev/null; then
    echo
    echo "开发环境已就绪："
    echo "  Web 前端: $VITE_URL"
    echo "  后端直连: http://localhost:7777/api"
    echo "  网关入口: http://localhost/api"
    echo "  Nginx 健康: http://localhost/nginx-health"
    echo
    echo "按 Ctrl+C 停止前端；Docker 栈会继续运行。"
    echo
    wait "$VITE_PID"
    exit $?
  fi
  sleep 1
done

echo "前端开发服务器在 60 秒内没有就绪。" >&2
exit 1
