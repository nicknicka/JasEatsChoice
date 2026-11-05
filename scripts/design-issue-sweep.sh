#!/usr/bin/env bash
# design-impl issue 心跳巡检：最后一条评论距今达到阈值即判超时，放回可领取队列。
#
# 契约见 docs/agents/design-overhaul-workflow.md 第 6 节。本脚本是该判定的唯一来源。
#
# 用法：
#   scripts/design-issue-sweep.sh                     # 实际执行
#   scripts/design-issue-sweep.sh --dry-run           # 只打印计划，不改任何 issue
#   SWEEP_TIMEOUT_MINUTES=30 scripts/design-issue-sweep.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

TIMEOUT_MINUTES="${SWEEP_TIMEOUT_MINUTES:-60}"
DRY_RUN=0
for arg in "$@"; do
  case "$arg" in
    --dry-run) DRY_RUN=1 ;;
    *) echo "未知参数：$arg" >&2; exit 2 ;;
  esac
done

NOW="$(date -u +%s)"

ISSUES="$(gh issue list --state open --label design-impl --limit 200 \
  --json number,title,assignees,comments)"

TOTAL="$(jq 'length' <<<"$ISSUES")"

# 超时判定：只看已接取的（有 assignee）。最后一条评论距今 >= 阈值算超时；
# 有 assignee 却一条评论都没有，等于没按契约发 CLAIM，同样算超时。
TIMED_OUT="$(jq -r --argjson now "$NOW" --argjson threshold "$TIMEOUT_MINUTES" '
  .[]
  | . as $issue
  | select(($issue.assignees | length) > 0)
  | (if ($issue.comments | length) == 0
     then null
     else ($issue.comments | map(.createdAt) | max)
     end) as $last_comment_at
  | (if $last_comment_at == null
     then -1
     else (((($now - ($last_comment_at | fromdateiso8601)) / 60) | floor))
     end) as $idle_minutes
  | select($last_comment_at == null or $idle_minutes >= $threshold)
  | [$issue.number, $idle_minutes, ($issue.assignees | map(.login) | join(",")), $issue.title]
  | @tsv
' <<<"$ISSUES")"

if [[ -z "$TIMED_OUT" ]]; then
  echo "巡检完成：扫描 $TOTAL 个 design-impl issue，超时 0 个。"
  exit 0
fi

SWEPT=0
while IFS=$'\t' read -r number idle_minutes assignees title; do
  [[ -z "$number" ]] && continue

  if [[ "$idle_minutes" == "-1" ]]; then
    reason="有 assignee 但没有任何评论，未按契约发 CLAIM"
  else
    reason="最后一条评论距今 ${idle_minutes} 分钟，超过阈值 ${TIMEOUT_MINUTES} 分钟"
  fi
  echo "超时 #${number} ${title}"
  echo "  ${reason}；assignee=${assignees}"

  if [[ "$DRY_RUN" == "1" ]]; then
    echo "  [dry-run] 移出 assignee、摘 design-impl、打 ready-for-agent、评论 [SWEEP]"
    continue
  fi

  IFS=',' read -r -a logins <<<"$assignees"
  for login in "${logins[@]}"; do
    gh issue edit "$number" --remove-assignee "$login" >/dev/null
  done
  gh issue edit "$number" --remove-label design-impl --add-label ready-for-agent >/dev/null
  gh issue comment "$number" --body "[SWEEP] $(date -u +%Y-%m-%dT%H:%M:%SZ) ${reason}，领取超时，已移出 assignee、摘除 design-impl 并放回 ready-for-agent 队列，可重新接取。" >/dev/null
  echo "  已放回队列"
  SWEPT=$((SWEPT + 1))
done <<<"$TIMED_OUT"

echo "巡检完成：扫描 $TOTAL 个 design-impl issue，超时 $SWEPT 个。"
