#!/usr/bin/env bash
# design-impl issue 心跳巡检：按工作流状态区分「待审查」与「进行中」，只回收确实停滞的 issue。
#
# 契约见 docs/agents/design-overhaul-workflow.md 第 5、6 节。本脚本是该判定的唯一来源。
#
# 判定（只对已接取 = 有 assignee 的 issue 生效）：
#   1. 停滞计时起点是**最后一条 [CLAIM] / [HEARTBEAT] 评论**，不是 issue 上任意评论
#      （主 agent 的验收评论、别人的提问会重置计时，掩盖真实停滞）。
#   2. 达到阈值后，先识别是否已进入「待审查」，两个信号任一命中即不回收：
#        - 最新一条 [HEARTBEAT] 的 status=待审查；
#        - [CLAIM] 里的 branch 已有 OPEN PR（gh pr list --head <branch> --state open）。
#   3. 有 assignee 却没有任何 CLAIM/HEARTBEAT 评论 = 没按契约发 CLAIM，同样判超时。
#
# 用法：
#   scripts/design-issue-sweep.sh                     # 实际执行
#   scripts/design-issue-sweep.sh --dry-run           # 只打印计划，不改任何 issue
#   SWEEP_TIMEOUT_MINUTES=30 scripts/design-issue-sweep.sh
#
# 离线验证用的注入点（默认不设置时行为不变，正常巡检无需关心）：
#   SWEEP_ISSUES_FILE=/path/issues.json   用 fixture 代替 gh issue list
#   SWEEP_NOW_EPOCH=1730000000            固定"现在"，让用例可复现
#   SWEEP_PR_LIST_CMD='some-cmd'          代替 gh pr list 查 OPEN PR（参数为 branch）
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

NOW="${SWEEP_NOW_EPOCH:-$(date -u +%s)}"

if [[ -n "${SWEEP_ISSUES_FILE:-}" ]]; then
  ISSUES="$(cat "$SWEEP_ISSUES_FILE")"
else
  ISSUES="$(gh issue list --state open --label design-impl --limit 200 \
    --json number,title,assignees,comments)"
fi

TOTAL="$(jq 'length' <<<"$ISSUES")"

# 输出每行一个候选（已接取且停滞）：编号 / 停滞分钟 / assignee / 标题 / 待审查心跳 / branch
STALE="$(jq -r --argjson now "$NOW" --argjson threshold "$TIMEOUT_MINUTES" '
  .[]
  | . as $issue
  | select(($issue.assignees | length) > 0)
  | ($issue.comments // []) as $comments
  | ($comments | map(select(.body | test("^\\s*\\[(CLAIM|HEARTBEAT)\\]")))) as $progress
  | (if ($progress | length) == 0 then null else ($progress | map(.createdAt) | max) end) as $last_progress_at
  | (if $last_progress_at == null
     then -1
     else ((($now - ($last_progress_at | fromdateiso8601)) / 60) | floor)
     end) as $idle_minutes
  | select($idle_minutes == -1 or $idle_minutes >= $threshold)
  | ($comments | map(select(.body | test("^\\s*\\[HEARTBEAT\\]")))) as $heartbeats
  | (if ($heartbeats | length) == 0
     then null
     else ($heartbeats | sort_by(.createdAt) | last)
     end) as $last_heartbeat
  | (if $last_heartbeat == null
     then false
     else ($last_heartbeat.body | test("status=\\s*待审查"))
     end) as $pending_heartbeat
  | ($comments | map(select(.body | test("^\\s*\\[CLAIM\\]")))) as $claims
  | (if ($claims | length) == 0
     then ""
     else ($claims | sort_by(.createdAt) | last | .body
           | (capture("branch=(?<branch>[^\\s,]+)") // {branch: ""}) | .branch)
     end) as $claim_branch
  | [$issue.number, $idle_minutes, ($issue.assignees | map(.login) | join(",")), $issue.title,
     (if $pending_heartbeat then "1" else "0" end), $claim_branch]
  | @tsv
' <<<"$ISSUES")"

# branch 是否已有 OPEN PR：打印 PR 编号，无则打印空。
open_pr_for_branch() {
  local branch="$1"
  if [[ -n "${SWEEP_PR_LIST_CMD:-}" ]]; then
    # shellcheck disable=SC2086  # 注入点允许写 "cmd arg" 形式，需要按词拆分
    $SWEEP_PR_LIST_CMD "$branch" </dev/null
  else
    gh pr list --head "$branch" --state open --json number \
      --jq 'if length > 0 then (.[0].number | tostring) else "" end' </dev/null
  fi
}

SWEPT=0
PENDING=0
while IFS=$'\t' read -r number idle_minutes assignees title pending_heartbeat claim_branch; do
  [[ -z "$number" ]] && continue

  if [[ "$idle_minutes" == "-1" ]]; then
    reason="有 assignee 但没有任何 CLAIM/HEARTBEAT 评论，未按契约发 CLAIM"
  else
    reason="最后一条 CLAIM/HEARTBEAT 评论距今 ${idle_minutes} 分钟，超过阈值 ${TIMEOUT_MINUTES} 分钟"
  fi

  # 先认「待审查」：agent 已收工不再发心跳，不能被当成停滞打回队列。
  if [[ "$pending_heartbeat" == "1" ]]; then
    echo "待审查 #${number} ${title}"
    echo "  最新心跳 status=待审查；不回收，等待主 agent 验收"
    PENDING=$((PENDING + 1))
    continue
  fi

  if [[ -n "$claim_branch" ]]; then
    open_pr="$(open_pr_for_branch "$claim_branch" || true)"
    if [[ -n "$open_pr" ]]; then
      echo "待审查 #${number} ${title}"
      echo "  分支 ${claim_branch} 已有 OPEN PR #${open_pr}；不回收，等待主 agent 验收"
      PENDING=$((PENDING + 1))
      continue
    fi
  fi

  echo "超时 #${number} ${title}"
  echo "  ${reason}；assignee=${assignees}"

  if [[ "$DRY_RUN" == "1" ]]; then
    echo "  [dry-run] 移出 assignee、摘 design-impl、打 ready-for-agent、评论 [SWEEP]"
    SWEPT=$((SWEPT + 1))
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
done <<<"$STALE"

echo "巡检完成：扫描 $TOTAL 个 design-impl issue，超时 $SWEPT 个，待审查 $PENDING 个。"
