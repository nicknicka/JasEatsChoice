#!/usr/bin/env bash
# 独立启动 / 停止 / 状态 / 日志（ticket #537）。
#
#   scripts/agent-runtime/dsh-ctl.sh start|stop|status|logs
#
# dsh 以专用账号运行，面板只监听 127.0.0.1，日志落在 DSH_LOG_DIR。
# 具体启动命令由 DSH_START_CMD 提供（不同 dsh 版本 CLI 不同，不写死在仓库里）：
#   DSH_START_CMD="dsh panel --host 127.0.0.1 --port 8099"
set -euo pipefail

DSH_USER="${DSH_USER:-dsh}"
DSH_HOME="${DSH_HOME:-/var/lib/dsh-agent}"
DSH_LOG_DIR="${DSH_LOG_DIR:-/var/log/dsh-agent}"
DSH_PID_FILE="${DSH_PID_FILE:-$DSH_LOG_DIR/dsh.pid}"
DSH_LOG_FILE="${DSH_LOG_FILE:-$DSH_LOG_DIR/dsh.log}"
DSH_START_CMD="${DSH_START_CMD:-}"
DSH_PANEL_HOST="${DSH_PANEL_HOST:-127.0.0.1}"
DSH_PANEL_PORT="${DSH_PANEL_PORT:-8099}"

as_dsh() {
  # HOME 必须显式指向专用 home，避免继承调用者的 home。
  if [[ "$(id -u)" == "0" ]]; then
    sudo -u "$DSH_USER" -H env HOME="$DSH_HOME" "$@"
  else
    env HOME="$DSH_HOME" "$@"
  fi
}

is_running() {
  [[ -f "$DSH_PID_FILE" ]] || return 1
  local pid
  pid="$(cat "$DSH_PID_FILE" 2>/dev/null || true)"
  [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null
}

cmd_start() {
  if is_running; then
    echo "dsh 已在运行（pid $(cat "$DSH_PID_FILE")）"
    return 0
  fi
  if [[ -z "$DSH_START_CMD" ]]; then
    echo "未设置 DSH_START_CMD，拒绝盲目启动。示例：" >&2
    echo '  DSH_START_CMD="dsh panel --host 127.0.0.1 --port 8099" scripts/agent-runtime/dsh-ctl.sh start' >&2
    return 2
  fi
  # shellcheck disable=SC2086  # 允许 DSH_START_CMD 带参数
  as_dsh sh -c "nohup $DSH_START_CMD >>'$DSH_LOG_FILE' 2>&1 & echo \$! >'$DSH_PID_FILE'"
  sleep 1
  if is_running; then
    echo "dsh 已启动（pid $(cat "$DSH_PID_FILE")，面板 $DSH_PANEL_HOST:$DSH_PANEL_PORT）"
  else
    echo "dsh 启动后立即退出，请看 $DSH_LOG_FILE" >&2
    return 1
  fi
}

cmd_stop() {
  if ! is_running; then
    echo "dsh 未在运行"
    rm -f "$DSH_PID_FILE"
    return 0
  fi
  local pid
  pid="$(cat "$DSH_PID_FILE")"
  kill "$pid" 2>/dev/null || true
  for _ in 1 2 3 4 5; do
    kill -0 "$pid" 2>/dev/null || break
    sleep 1
  done
  kill -9 "$pid" 2>/dev/null || true
  rm -f "$DSH_PID_FILE"
  echo "dsh 已停止"
}

cmd_status() {
  if is_running; then
    echo "运行中：pid $(cat "$DSH_PID_FILE")"
  else
    echo "未运行"
  fi
  echo "监听检查（只应看到 $DSH_PANEL_HOST:$DSH_PANEL_PORT）："
  if command -v ss >/dev/null 2>&1; then
    ss -ltnp 2>/dev/null | grep ":$DSH_PANEL_PORT" || echo "  端口 $DSH_PANEL_PORT 未监听"
  elif command -v lsof >/dev/null 2>&1; then
    lsof -nP -iTCP:"$DSH_PANEL_PORT" -sTCP:LISTEN 2>/dev/null || echo "  端口 $DSH_PANEL_PORT 未监听"
  else
    echo "  缺少 ss/lsof，无法检查端口"
  fi
  # 只要出现非回环地址监听就判失败：面板不得暴露到网络。
  if command -v ss >/dev/null 2>&1 && ss -ltn 2>/dev/null | grep ":$DSH_PANEL_PORT " | grep -qvE '127\.0\.0\.1|\[::1\]'; then
    echo "警告：面板端口在非回环地址上监听，违反隔离要求" >&2
    return 1
  fi
}

cmd_logs() {
  if [[ -f "$DSH_LOG_FILE" ]]; then
    tail -n "${DSH_LOG_LINES:-200}" "$DSH_LOG_FILE"
  else
    echo "暂无日志：$DSH_LOG_FILE" >&2
    return 1
  fi
}

case "${1:-}" in
  start) cmd_start ;;
  stop) cmd_stop ;;
  status) cmd_status ;;
  logs) cmd_logs ;;
  *) echo "用法: $0 start|stop|status|logs" >&2; exit 2 ;;
esac
