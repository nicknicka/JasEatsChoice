#!/usr/bin/env bash
# dsh 升级流程（ticket #541）：备份 -> 升级 -> 探针 -> 失败自动回退。
#
#   [sudo] scripts/agent-runtime/upgrade.sh
#
# 不做原地降级：新版迁移过 state 后，回退靠恢复升级前备份（rollback.sh）。
# 记录写到 $DSH_LOG_DIR/upgrade-records/（仓库外），模板见 docs/agent-runtime/upgrade-record-template.md。
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=versions.env
source "$HERE/versions.env"

DSH_USER="${DSH_USER:-dsh}"
DSH_HOME="${DSH_HOME:-/var/lib/dsh-agent}"
DSH_LOG_DIR="${DSH_LOG_DIR:-/var/log/dsh-agent}"
DSH_STATE_DIR="${DSH_STATE_DIR:-$DSH_HOME/state}"
DSH_PROFILE_DIR="${DSH_PROFILE_DIR:-$DSH_HOME/.config/dsh}"
DSH_REPO="${DSH_REPO:-/srv/dsh-agent/repo}"

for pin in "$DSH_SUPERAGENT_COMMIT" "$DSH_VERSION"; do
  case "$pin" in
    CHANGE_ME_*|"")
      echo "versions.env 里 $pin 仍是占位符：先填真实 release 再升级" >&2
      exit 2
      ;;
  esac
done

TS="$(date +%Y%m%d-%H%M%S)"
BACKUP_DIR="$DSH_LOG_DIR/upgrade-backups/$TS"
RECORD_DIR="$DSH_LOG_DIR/upgrade-records"
sudo mkdir -p "$BACKUP_DIR" "$RECORD_DIR"

echo "==> 1/5 备份 stateDir 与 profile（升级前快照）"
sudo tar -C "$DSH_STATE_DIR" -czf "$BACKUP_DIR/state.tgz" .
sudo tar -C "$DSH_PROFILE_DIR" -czf "$BACKUP_DIR/profile.tgz" .
echo "备份：$BACKUP_DIR"

echo "==> 2/5 升级到锁定版本 dsh=$DSH_VERSION commit=$DSH_SUPERAGENT_COMMIT"
sudo -u "$DSH_USER" -H env HOME="$DSH_HOME" bash -c "
  set -euo pipefail
  cd '$DSH_REPO'
  git fetch --all --tags
  git checkout '$DSH_SUPERAGENT_COMMIT'
  corepack enable >/dev/null 2>&1 || true
  corepack prepare pnpm@$PNPM_VERSION --activate >/dev/null 2>&1 || true
  pnpm install --frozen-lockfile
  pnpm build
"

echo "==> 3/5 运行探针"
if ! "$HERE/probe.sh"; then
  echo "==> 探针失败，自动回退"
  if [[ "$(id -u)" == "0" ]]; then
    "$HERE/rollback.sh" "$BACKUP_DIR"
  else
    sudo "$HERE/rollback.sh" "$BACKUP_DIR"
  fi
  echo "已回退。请记录失败原因到 $RECORD_DIR 并停止升级。" >&2
  exit 1
fi

echo "==> 4/5 写验证记录"
cat > "$RECORD_DIR/$TS.md" <<REC
# dsh 升级记录 $TS

- dsh-superagent: $DSH_SUPERAGENT_COMMIT
- DSH: $DSH_VERSION
- Node major: $NODE_MAJOR
- pnpm: $PNPM_VERSION
- 备份目录: $BACKUP_DIR
- 探针: dsh 可用 / 服务在跑 / Management API(state+sources) 全部通过

回退方式：scripts/agent-runtime/rollback.sh $BACKUP_DIR（不原地降级）
REC
echo "记录：$RECORD_DIR/$TS.md"

echo "==> 5/5 完成（Node $NODE_MAJOR / pnpm $PNPM_VERSION 需与本机一致）"
