#!/usr/bin/env bash
# 升级后的探针套件（ticket #541 / #557）：dsh 可用 / 服务在跑 / Management API 可达。
#
#   scripts/agent-runtime/probe.sh
#
# 任一探针失败退出非零，upgrade.sh 会据此触发回退。探针只读，不修改 dsh 状态。
#
# 真实 dsh **没有** panel/status/auth/source/policy/model 子命令：
#   - 服务在跑 → 用 dsh-superagent 自带的 scripts/dsh-superagent status（退出码 0 为正常）
#   - 健康 / 认证 / Source / Policy / 模型 → 从 Management API 的 state / sources 读
# 具体命令由 DSH_PROBE_* 覆盖（值是 shell 命令串，可带参数）。
#
# 端口：本机按项目分实例，JAS=3083、bilistu=3082；8099 只属于 Linux 目标形态，别当默认值。
# 未显式给 DSH_MANAGEMENT_BASE_URL 时，按 DSH_PANEL_CANDIDATES 顺序探测，取第一个有响应的。
set -uo pipefail

DSH_BIN="${DSH_BIN:-dsh}"
DSH_PANEL_CANDIDATES="${DSH_PANEL_CANDIDATES:-3083 3082}"
HDR='x-orchestrator-request: 1'

resolve_repo() {
  if [[ -n "${DSH_REPO:-}" ]]; then printf '%s' "$DSH_REPO"; return 0; fi
  local cand
  for cand in /srv/dsh-agent/repo "$HOME/src/dsh-superagent"; do
    if [[ -d "$cand" ]]; then printf '%s' "$cand"; return 0; fi
  done
  printf '%s' /srv/dsh-agent/repo
}

resolve_base_url() {
  if [[ -n "${DSH_MANAGEMENT_BASE_URL:-}" ]]; then
    printf '%s' "${DSH_MANAGEMENT_BASE_URL%/}"; return 0
  fi
  local port
  for port in $DSH_PANEL_CANDIDATES; do
    if curl -fsS -m 2 -H "$HDR" "http://127.0.0.1:$port/orchestrator/api/state" >/dev/null 2>&1; then
      printf 'http://127.0.0.1:%s' "$port"; return 0
    fi
  done
  # 一个都不通时给出首选端口，让后续探针自己 FAIL 并打印真实报错。
  printf 'http://127.0.0.1:%s' "${DSH_PANEL_CANDIDATES%% *}"
}

DSH_REPO="$(resolve_repo)"
API="$(resolve_base_url)/orchestrator/api"

failures=0
run() {  # $1 名称；$2 shell 命令串（只读）
  if eval "$2" >/dev/null 2>&1; then
    echo "  PASS  $1"
  else
    echo "  FAIL  $1"
    failures=$((failures + 1))
  fi
}

echo "== 升级探针 =="
echo "  Management API: $API"
echo "  DSH repo:       $DSH_REPO"
run "dsh 可用"        "${DSH_PROBE_BIN:-"$DSH_BIN --version"}"
run "服务在跑"        "${DSH_PROBE_SERVICE:-"$DSH_REPO/scripts/dsh-superagent status"}"
run "Management API state"   "${DSH_PROBE_STATE:-"curl -fsS -H '$HDR' '$API/state'"}"
run "Management API sources" "${DSH_PROBE_SOURCES:-"curl -fsS -H '$HDR' '$API/sources'"}"

echo
if [[ "$failures" -gt 0 ]]; then
  echo "探针失败：$failures 项，应执行回退"
  exit 1
fi
echo "探针全部通过"
