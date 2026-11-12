#!/usr/bin/env bash
# 一键切回旧配置快照（ticket #541）。
#
#   [sudo] scripts/agent-runtime/restore-config.sh <snapshot-dir>
set -euo pipefail

DSH_USER="${DSH_USER:-dsh}"
DSH_HOME="${DSH_HOME:-/var/lib/dsh-agent}"
DSH_PROFILE_DIR="${DSH_PROFILE_DIR:-$DSH_HOME/.config/dsh}"
SNAP="${1:-}"
if [[ -z "$SNAP" || ! -f "$SNAP/profile.tgz" ]]; then
  echo "用法: $0 <含 profile.tgz 的快照目录>" >&2
  exit 2
fi

echo "==> 停止 dsh 后恢复 profile"
"$(dirname "$0")/dsh-ctl.sh" stop || true
sudo rm -rf "$DSH_PROFILE_DIR"
sudo mkdir -p "$DSH_PROFILE_DIR"
sudo tar -C "$DSH_PROFILE_DIR" -xzf "$SNAP/profile.tgz"
sudo chown -R "$DSH_USER":"$DSH_USER" "$DSH_PROFILE_DIR"
echo "已切回：$SNAP"
