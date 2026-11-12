#!/usr/bin/env bash
# 回退到升级前备份（ticket #541）。
#
#   scripts/agent-runtime/rollback.sh <backup-dir>
#
# 说明：新版完成 state 迁移后，旧版**打不开**新 state。因此回退只恢复升级前的
# stateDir 与 profile 备份，不做原地降级。
set -euo pipefail

DSH_USER="${DSH_USER:-dsh}"
DSH_HOME="${DSH_HOME:-/var/lib/dsh-agent}"
DSH_STATE_DIR="${DSH_STATE_DIR:-$DSH_HOME/state}"
DSH_PROFILE_DIR="${DSH_PROFILE_DIR:-$DSH_HOME/.config/dsh}"

BACKUP_DIR="${1:-}"
if [[ -z "$BACKUP_DIR" || ! -d "$BACKUP_DIR" ]]; then
  echo "用法: $0 <升级前备份目录>" >&2
  exit 2
fi

as_dsh() {
  if [[ "$(id -u)" == "0" ]]; then
    sudo -u "$DSH_USER" -H env HOME="$DSH_HOME" "$@"
  else
    env HOME="$DSH_HOME" "$@"
  fi
}

echo "==> 停止 dsh"
"$(dirname "$0")/dsh-ctl.sh" stop || true

echo "==> 恢复 stateDir"
sudo rm -rf "$DSH_STATE_DIR"
sudo mkdir -p "$DSH_STATE_DIR"
sudo tar -C "$BACKUP_DIR" -xzf "$BACKUP_DIR/state.tgz" -C "$DSH_STATE_DIR"

echo "==> 恢复 profile"
sudo rm -rf "$DSH_PROFILE_DIR"
sudo mkdir -p "$DSH_PROFILE_DIR"
sudo tar -C "$BACKUP_DIR" -xzf "$BACKUP_DIR/profile.tgz" -C "$DSH_PROFILE_DIR"

sudo chown -R "$DSH_USER":"$DSH_USER" "$DSH_STATE_DIR" "$DSH_PROFILE_DIR"
echo "回退完成：已恢复 $BACKUP_DIR（未做原地降级）"
