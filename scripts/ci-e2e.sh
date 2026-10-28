#!/usr/bin/env bash
# Bring the dev stack + web dev server up, run Playwright against them, tear down.
# Shared by the smoke and regression workflows so CI and `make test` use one path.
#
#   scripts/ci-e2e.sh <spec> [<spec> ...]
#   scripts/ci-e2e.sh all            # 全部 114 条
#
# Everything binds 127.0.0.1 on purpose: nginx serves `listen 80` (IPv4) and Vite
# inherits that, so a "localhost" base URL can resolve to ::1 first and be refused.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ $# -eq 0 ]]; then
  echo "usage: $0 <spec ...> | all" >&2
  exit 2
fi

SPECS=("$@")
ALL_TESTS=0
if [[ ${#SPECS[@]} -eq 1 && "${SPECS[0]}" == "all" ]]; then
  ALL_TESTS=1
fi

export COMPOSE_FLAGS="--env-file deploy/.env -f deploy/compose.yml -f deploy/compose.dev.yml"
export API_PROXY_TARGET="http://127.0.0.1:7777"
export WS_PROXY_TARGET="http://127.0.0.1:11277"
export VITE_DEV_HOST="127.0.0.1"
export VITE_DEV_PORT="5173"
export PLAYWRIGHT_BASE_URL="http://127.0.0.1:5173"
export TEST_API_BASE="http://127.0.0.1:7777/api"

VITE_PID=""
cleanup() {
  if [[ -n "$VITE_PID" ]]; then
    kill "$VITE_PID" 2>/dev/null || true
    wait "$VITE_PID" 2>/dev/null || true
  fi
  if [[ "${KEEP_STACK:-0}" != "1" ]]; then
    docker compose $COMPOSE_FLAGS down -v >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

echo "==> 1/5 构建并启动容器栈"
# CI 没有 deploy/.env（密钥不入库），用一次性随机值凑齐 compose 的必填校验
if [[ ! -f deploy/.env ]]; then
  echo "  deploy/.env 不存在，生成一次性凭据"
  bash scripts/gen-env.sh
fi
docker compose $COMPOSE_FLAGS up -d --build

echo "==> 2/5 等待服务健康"
WAIT_TIMEOUT="${WAIT_TIMEOUT:-600}" scripts/wait-healthy.sh mysql redis spring-boot nginx

echo "==> 3/5 种子与账号自检"
curl -fsS http://127.0.0.1:7777/api/actuator/health | grep -q '"status":"UP"'
# 用例的登录夹具依赖这两个账号，先在这里确认，免得 114 条一起红
curl -fsS -X POST http://127.0.0.1:7777/api/admin/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | grep -q '"success":true'
echo "  管理员登录 OK"

echo "==> 4/5 启动 Web 渲染进程 dev server"
(cd apps/client && npm run dev:web > /tmp/vite.log 2>&1) &
VITE_PID=$!
for _ in $(seq 1 60); do
  if curl -fsS -o /dev/null "$PLAYWRIGHT_BASE_URL/"; then break; fi
  sleep 2
done
curl -fsS -o /dev/null "$PLAYWRIGHT_BASE_URL/" || {
  echo "  5173 没有起来，vite 日志：" >&2
  tail -30 /tmp/vite.log >&2
  exit 1
}
echo "  $PLAYWRIGHT_BASE_URL 可访问"

echo "==> 5/5 跑用例：$([[ $ALL_TESTS -eq 1 ]] && echo 全部 114 条 || echo "${SPECS[*]}")"
cd tests
if [[ $ALL_TESTS -eq 1 ]]; then
  npx playwright test
else
  npx playwright test "${SPECS[@]}"
fi
