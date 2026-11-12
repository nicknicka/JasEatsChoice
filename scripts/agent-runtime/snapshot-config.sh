#!/usr/bin/env bash
# 切换模型/插件前保存配置快照（ticket #541）。
#
#   [sudo] scripts/agent-runtime/snapshot-config.sh [label]
#
# 只在切换“之前”调用；切换只影响后续 Ticket（正在跑的任务不受影响）。
# 一键切回用 restore-config.sh。
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DSH_HOME="${DSH_HOME:-/var/lib/dsh-agent}"
DSH_LOG_DIR="${DSH_LOG_DIR:-/var/log/dsh-agent}"
DSH_PROFILE_DIR="${DSH_PROFILE_DIR:-$DSH_HOME/.config/dsh}"
LABEL="${1:-manual}"

TS="$(date +%Y%m%d-%H%M%S)"
SNAP="$DSH_LOG_DIR/config-snapshots/$TS-$LABEL"
sudo mkdir -p "$SNAP"
sudo tar -C "$DSH_PROFILE_DIR" -czf "$SNAP/profile.tgz" .
{
  echo "created_at=$TS"
  echo "label=$LABEL"
  # 记录锁定版本，便于确认快照对应的运行时组合。
  grep -E '^(DSH_SUPERAGENT_COMMIT|DSH_VERSION|NODE_MAJOR|PNPM_VERSION)=' "$HERE/versions.env" || true
} | sudo tee "$SNAP/manifest.txt" >/dev/null
echo "快照：$SNAP"
echo "切回：scripts/agent-runtime/restore-config.sh $SNAP"
