#!/usr/bin/env bash
# 专用 Agent OS 账号与目录初始化（ticket #537）。
#
# 目标：dsh-superagent 在同机专用账号 `dsh` 的专用目录下运行，读不到开发者
# home、deploy/.env、生产上传卷、Docker socket、SSH key 或云凭据。
# 本脚本需要 root（用 sudo 执行），幂等：重复运行只会补齐缺失部分。
#
#   sudo scripts/agent-runtime/install-account.sh
#
# 账号与目录约定（脚本与 verify-isolation.sh 共用，改这里要同步文档）：
#   DSH_USER=dsh            专用 OS 账号（无 docker 组、nologin 交互 shell）
#   DSH_HOME=/var/lib/dsh-agent   账号 home（状态、worktree、凭据都在这里）
#   DSH_LOG_DIR=/var/log/dsh-agent  运行日志
#   DSH_REPO=/srv/dsh-agent/repo    仓库克隆与 worktree 根
set -euo pipefail

DSH_USER="${DSH_USER:-dsh}"
DSH_HOME="${DSH_HOME:-/var/lib/dsh-agent}"
DSH_LOG_DIR="${DSH_LOG_DIR:-/var/log/dsh-agent}"
DSH_REPO="${DSH_REPO:-/srv/dsh-agent/repo}"
DSH_GROUP="${DSH_GROUP:-$DSH_USER}"

if [[ "$(id -u)" != "0" ]]; then
  echo "必须以 root 运行（sudo $0）" >&2
  exit 1
fi

if ! id "$DSH_USER" >/dev/null 2>&1; then
  echo "==> 创建专用账号 $DSH_USER"
  # --system：系统账号，不带过期策略；shell 设为 nologin，只允许 sudo -u 显式调用。
  # 刻意 NOT 加入 docker 组：账号拿不到 Docker socket。
  useradd --system --create-home --home-dir "$DSH_HOME" --shell /usr/sbin/nologin "$DSH_USER"
else
  echo "==> 账号 $DSH_USER 已存在，复用"
fi

echo "==> 准备专用目录"
install -d -m 0700 -o "$DSH_USER" -g "$DSH_GROUP" "$DSH_HOME"
install -d -m 0700 -o "$DSH_USER" -g "$DSH_GROUP" "$DSH_HOME/state"
install -d -m 0700 -o "$DSH_USER" -g "$DSH_GROUP" "$DSH_HOME/worktree"
install -d -m 0700 -o "$DSH_USER" -g "$DSH_GROUP" "$DSH_HOME/.config"
install -d -m 0750 -o root -g "$DSH_USER" "$DSH_LOG_DIR"
install -d -m 0750 -o "$DSH_USER" -g "$DSH_GROUP" "$DSH_REPO"

echo "==> 收紧 home 权限（组/其他不可读）"
chmod 0700 "$DSH_HOME"

cat <<EOF
完成。后续手工步骤（需要你提供真实凭据，脚本不代填）：

1. 用专用 GitHub 账号登录 gh，且配置落在 dsh 的 home：
     sudo -u $DSH_USER -H env HOME=$DSH_HOME gh auth login --hostname github.com
   确认没有复用个人登录：sudo -u $DSH_USER -H gh auth status

2. 模型 key 只写 $DSH_HOME/.config/model.key（0600，属主 $DSH_USER），
   不要写进 deploy/.env，也不要写进仓库。

3. 运行隔离验证清单：
     sudo scripts/agent-runtime/verify-isolation.sh

边界：不要把生产数据库、生产上传卷或生产服务放到这台机器上（见 docs/agent-runtime/README.md）。
EOF
