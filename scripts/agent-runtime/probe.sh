#!/usr/bin/env bash
# 升级后的探针套件（ticket #541）：健康 / 认证 / Source / Policy / 模型。
#
#   scripts/agent-runtime/probe.sh
#
# 任一探针失败退出非零，upgrade.sh 会据此触发回退。
# 具体命令由 DSH_PROBE_* 覆盖，默认用 dsh 自带 CLI；探针只读，不修改 dsh 状态。
set -uo pipefail

DSH_BIN="${DSH_BIN:-dsh}"
failures=0
check() {
  local name="$1"; shift
  if "$@" >/dev/null 2>&1; then
    echo "  PASS  $name"
  else
    echo "  FAIL  $name"
    failures=$((failures + 1))
  fi
}

echo "== 升级探针 =="
check "健康" "${DSH_PROBE_HEALTH:-"$DSH_BIN" panel health}"
check "认证" "${DSH_PROBE_AUTH:-"$DSH_BIN" auth status}"
check "Source" "${DSH_PROBE_SOURCE:-"$DSH_BIN" source status}"
check "Policy" "${DSH_PROBE_POLICY:-"$DSH_BIN" policy status}"
check "模型" "${DSH_PROBE_MODEL:-"$DSH_BIN" model probe}"

echo
if [[ "$failures" -gt 0 ]]; then
  echo "探针失败：$failures 项，应执行回退"
  exit 1
fi
echo "探针全部通过"
