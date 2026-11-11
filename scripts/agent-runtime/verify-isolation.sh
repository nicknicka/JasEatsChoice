#!/usr/bin/env bash
# 可重复执行的隔离验证清单（ticket #537）。
#
#   [sudo] scripts/agent-runtime/verify-isolation.sh
#
# 逐条验证专用账号 dsh 读不到它不该读的东西，并确认面板只监听回环地址。
# 任一条 FAIL 退出非零。非 root 运行时会跳过需要 sudo -u dsh 的探测并标记 SKIP。
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DSH_USER="${DSH_USER:-dsh}"
DSH_HOME="${DSH_HOME:-/var/lib/dsh-agent}"
DSH_PANEL_PORT="${DSH_PANEL_PORT:-8099}"
DEV_ENV_PATH="${DEV_ENV_PATH:-$REPO_ROOT/deploy/.env}"
DEV_HOME="${DEV_HOME:-$HOME}"
DOCKER_SOCK="${DOCKER_SOCK:-/var/run/docker.sock}"

failures=0
pass() { echo "  PASS  $1"; }
fail() { echo "  FAIL  $1"; failures=$((failures + 1)); }
skip() { echo "  SKIP  $1"; }

can_sudo() { [[ "$(id -u)" == "0" ]] && id "$DSH_USER" >/dev/null 2>&1; }

# 以 dsh 身份执行 test，返回 0 表示「读得到」（即隔离失败）。
dsh_can_read() {
  local target="$1"
  sudo -u "$DSH_USER" -H env HOME="$DSH_HOME" test -r "$target"
}

echo "== 账号与目录 =="
if id "$DSH_USER" >/dev/null 2>&1; then
  pass "账号 $DSH_USER 存在"
else
  fail "账号 $DSH_USER 不存在（先运行 install-account.sh）"
fi

if id -nG "$DSH_USER" 2>/dev/null | tr ' ' '\n' | grep -qx docker; then
  fail "$DSH_USER 属于 docker 组（应移除，Docker socket 等于 root）"
else
  pass "$DSH_USER 不在 docker 组"
fi

if [[ -d "$DSH_HOME" ]]; then
  perms="$(stat -c '%a' "$DSH_HOME" 2>/dev/null || stat -f '%Lp' "$DSH_HOME")"
  if [[ "$perms" == "700" ]]; then
    pass "$DSH_HOME 权限为 700"
  else
    fail "$DSH_HOME 权限为 $perms（应为 700）"
  fi
else
  fail "$DSH_HOME 不存在"
fi

echo "== 不可读边界 =="
canary="$DEV_HOME/.dsh-isolation-canary"
if can_sudo; then
  printf 'canary' > "$canary" 2>/dev/null && chmod 600 "$canary" 2>/dev/null || true
  if dsh_can_read "$canary"; then
    fail "$DSH_USER 能读到 $DEV_HOME 下的文件（开发者 home 未隔离）"
  else
    pass "$DSH_USER 读不到开发者 home 文件"
  fi
  rm -f "$canary"

  if [[ -e "$DEV_ENV_PATH" ]]; then
    if dsh_can_read "$DEV_ENV_PATH"; then
      fail "$DSH_USER 能读到 $DEV_ENV_PATH（凭据泄漏）"
    else
      pass "$DSH_USER 读不到 deploy/.env"
    fi
  else
    skip "未找到 $DEV_ENV_PATH"
  fi

  if [[ -e "$DOCKER_SOCK" ]]; then
    if dsh_can_read "$DOCKER_SOCK"; then
      fail "$DSH_USER 能访问 Docker socket（等于宿主机 root）"
    else
      pass "$DSH_USER 访问不到 Docker socket"
    fi
  else
    skip "未找到 $DOCKER_SOCK"
  fi

  ssh_dir="$DSH_HOME/.ssh"
  if [[ -d "$ssh_dir" ]] && find "$ssh_dir" -type f -name 'id_*' 2>/dev/null | grep -q .; then
    fail "$DSH_HOME/.ssh 存在私钥（专用账号不应持有开发者 SSH key）"
  else
    pass "$DSH_HOME/.ssh 无私钥"
  fi
else
  skip "非 root 或账号缺失，跳过可读性探测（用 sudo 重跑以获得完整结论）"
fi

echo "== 面板监听 =="
if command -v ss >/dev/null 2>&1; then
  listeners="$(ss -ltn 2>/dev/null | grep ":$DSH_PANEL_PORT " || true)"
  if [[ -z "$listeners" ]]; then
    skip "端口 $DSH_PANEL_PORT 当前未监听（dsh 可能未启动）"
  elif echo "$listeners" | grep -qvE '127\.0\.0\.1|\[::1\]'; then
    fail "端口 $DSH_PANEL_PORT 在非回环地址监听"
  else
    pass "端口 $DSH_PANEL_PORT 仅回环监听"
  fi
else
  skip "缺少 ss，无法检查监听地址"
fi

echo
if [[ "$failures" -gt 0 ]]; then
  echo "隔离验证失败：$failures 项"
  exit 1
fi
echo "隔离验证通过（跳过项需在有权限的环境复跑）"
