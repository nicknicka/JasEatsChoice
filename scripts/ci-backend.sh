#!/usr/bin/env bash
# Bring up only the dependencies the backend suite needs, run `./mvnw test`, then
# tear the stack down. Shared by the backend CI job and `make test-backend`.
#
#   scripts/ci-backend.sh
#
# Unlike ci-e2e.sh this never starts the spring-boot container: the tests build
# their own application contexts, and a running app would fight them for ports.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

export COMPOSE_FLAGS="--env-file deploy/.env -f deploy/compose.yml -f deploy/compose.dev.yml"

cleanup() {
  if [[ "${KEEP_STACK:-0}" != "1" ]]; then
    docker compose $COMPOSE_FLAGS down -v >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

echo "==> 1/3 准备凭据"
# CI 没有 deploy/.env（密钥不入库），用一次性随机值凑齐 compose 的必填校验
if [[ ! -f deploy/.env ]]; then
  echo "  deploy/.env 不存在，生成一次性凭据"
  bash scripts/gen-env.sh
fi

# 测试跑在宿主机上，库凭据由 .env 提供；compose.dev 已把 mysql/redis/qdrant 的
# 端口映射到 127.0.0.1，所以测试直接连本机即可。
set -a
# shellcheck disable=SC1091
source deploy/.env
set +a

echo "==> 2/3 启动依赖（mysql/redis/qdrant，不含应用本身）"
docker compose $COMPOSE_FLAGS up -d mysql redis qdrant
WAIT_TIMEOUT="${WAIT_TIMEOUT:-300}" scripts/wait-healthy.sh mysql redis qdrant

echo "==> 3/3 跑后端单测"
cd apps/server
./mvnw test
