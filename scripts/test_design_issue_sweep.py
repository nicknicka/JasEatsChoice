#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""design-issue-sweep.sh 判定逻辑的离线单元测试（标准库 unittest，零第三方依赖）。

不碰真实 GitHub：用 fixture JSON 喂 issue 列表，用假的 pr list 命令喂 OPEN PR 判定，
时间戳通过 SWEEP_NOW_EPOCH 固定，所以结果可复现。

跑法：在仓库根目录执行
    python3 -m unittest discover -s scripts -p 'test_*.py' -v
"""
import json
import os
import stat
import subprocess
import tempfile
import time
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SWEEP_SCRIPT = os.path.join(REPO_ROOT, "scripts", "design-issue-sweep.sh")

MINUTE = 60
# 固定"现在"，所有评论时间都相对它偏移。
NOW_EPOCH = int(time.mktime(time.strptime("2026-11-06T12:00:00", "%Y-%m-%dT%H:%M:%S")))


def iso(minutes_ago):
    """把"距今多少分钟"换成脚本能解析的 UTC ISO8601 时间戳。"""
    return time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime(NOW_EPOCH - minutes_ago * MINUTE))


def comment(minutes_ago, body):
    return {"createdAt": iso(minutes_ago), "body": body}


def issue(number, title, comments, assignees=("agent-1",)):
    return {
        "number": number,
        "title": title,
        "assignees": [{"login": login} for login in assignees],
        "comments": comments,
    }


def claim(minutes_ago, branch):
    return comment(
        minutes_ago,
        "[CLAIM] agent=agent-1 worktree=/tmp/wt branch={} plan=apps/client/x.vue".format(branch),
    )


def heartbeat(minutes_ago, status, note="进展正常"):
    return comment(minutes_ago, "[HEARTBEAT] {} status={} note={}".format(iso(minutes_ago), status, note))


class SweepTestCase(unittest.TestCase):
    """公共夹具：写 fixture、造假的 gh pr list，跑脚本，返回 stdout。"""

    def run_sweep(self, issues, open_prs=None, dry_run=True, timeout_minutes=60):
        """open_prs: {branch: pr_number}，命中即视为该分支有 OPEN PR。"""
        open_prs = open_prs or {}
        with tempfile.TemporaryDirectory() as tmp:
            issues_path = os.path.join(tmp, "issues.json")
            with open(issues_path, "w", encoding="utf-8") as handle:
                json.dump(issues, handle, ensure_ascii=False)

            pr_cmd_path = os.path.join(tmp, "fake_pr_list.sh")
            with open(pr_cmd_path, "w", encoding="utf-8") as handle:
                handle.write("#!/bin/sh\n")
                for branch, pr_number in open_prs.items():
                    handle.write('if [ "$1" = "{}" ]; then echo {}; exit 0; fi\n'.format(branch, pr_number))
                handle.write("exit 0\n")
            os.chmod(pr_cmd_path, os.stat(pr_cmd_path).st_mode | stat.S_IEXEC)

            env = dict(
                os.environ,
                SWEEP_ISSUES_FILE=issues_path,
                SWEEP_NOW_EPOCH=str(NOW_EPOCH),
                SWEEP_PR_LIST_CMD=pr_cmd_path,
                SWEEP_TIMEOUT_MINUTES=str(timeout_minutes),
            )
            proc = subprocess.run(
                ["bash", SWEEP_SCRIPT] + (["--dry-run"] if dry_run else []),
                cwd=REPO_ROOT,
                env=env,
                capture_output=True,
                text=True,
            )
            self.assertEqual(proc.returncode, 0, proc.stderr)
            return proc.stdout


class TestPendingReviewIsNotSwept(SweepTestCase):
    """issue #281 场景二/三：已进「待审查」的 issue 不能被打回队列。"""

    def test_pending_review_with_open_pr_is_not_reclaimed(self):
        # 心跳还是进行中（agent 收工前最后一条），但 CLAIM 分支已开 OPEN PR，
        # 评论已静默 713 分钟 —— 这正是 #265/#266/#269 被误判的形状。
        issues = [
            issue(
                269,
                "[设计改造·L1] RowActions",
                [
                    claim(800, "codex/269-rowactions"),
                    heartbeat(720, "进行中", note="组件写完，正在起渲染图"),
                    comment(713, "主 agent 的验收评论，与状态无关"),
                ],
            )
        ]
        out = self.run_sweep(issues, open_prs={"codex/269-rowactions": 279})
        self.assertIn("待审查 #269", out)
        self.assertIn("已有 OPEN PR #279", out)
        self.assertNotIn("超时 #269", out)
        self.assertIn("超时 0 个，待审查 1 个", out)

    def test_pending_review_heartbeat_is_enough_without_open_pr(self):
        # 信号一单独命中：最新心跳 status=待审查，即使查不到 OPEN PR 也不回收。
        issues = [
            issue(
                266,
                "[设计改造·L1] AppPageHeader",
                [claim(800, "codex/266-pageheader"), heartbeat(700, "待审查", note="PR #277 已推送")],
            )
        ]
        out = self.run_sweep(issues, open_prs={})
        self.assertIn("待审查 #266", out)
        self.assertIn("最新心跳 status=待审查", out)
        self.assertNotIn("超时 #266", out)

    def test_pending_review_with_merged_pr_is_not_reclaimed(self):
        # 场景三：PR 已合并 → 查询不到 OPEN PR，只剩心跳信号。issue 还开着是因为关单尚未发生，
        # 该等主 agent 关单，而不是放回 design-impl 队列重做同一个组件。
        issues = [
            issue(
                265,
                "[设计改造·L1] AppStatCard",
                [
                    claim(900, "codex/265-statcard"),
                    heartbeat(800, "进行中", note="开始实现"),
                    heartbeat(716, "待审查", note="PR #278 已推送"),
                    comment(300, "已合并：PR #278 → squash（主 agent 验收评论）"),
                ],
            )
        ]
        out = self.run_sweep(issues, open_prs={})
        self.assertIn("待审查 #265", out)
        self.assertNotIn("超时 #265", out)
        self.assertIn("超时 0 个，待审查 1 个", out)


class TestInProgressTimeoutIsSwept(SweepTestCase):
    """issue #281 场景一：确实停滞的进行中任务仍要回收。"""

    def test_in_progress_and_stalled_is_swept(self):
        issues = [
            issue(
                282,
                "[设计改造·L1补] 危险确认按钮",
                [claim(300, "codex/282-danger"), heartbeat(200, "进行中", note="在改样式")],
            )
        ]
        out = self.run_sweep(issues, open_prs={})
        self.assertIn("超时 #282", out)
        self.assertIn("最后一条 CLAIM/HEARTBEAT 评论距今 200 分钟", out)
        self.assertIn("[dry-run] 移出 assignee、摘 design-impl", out)
        self.assertIn("超时 1 个，待审查 0 个", out)
        self.assertNotIn("待审查 #282", out)

    def test_newer_heartbeat_status_keeps_timer_running(self):
        # 最新一条心跳是进行中，即便更早的心心跳过待审查，仍按进行中判。
        issues = [
            issue(
                283,
                "[设计改造·L2] public/01-login",
                [
                    claim(400, "codex/283-login"),
                    heartbeat(300, "待审查", note="PR #290 已推送"),
                    heartbeat(150, "进行中", note="验收打回，继续改"),
                ],
            )
        ]
        out = self.run_sweep(issues, open_prs={})
        self.assertIn("超时 #283", out)
        self.assertNotIn("待审查 #283", out)

    def test_assigned_without_claim_is_swept(self):
        # 有 assignee 却没有任何 CLAIM/HEARTBEAT：未按契约发 CLAIM，判超时（文案保持原样）。
        issues = [issue(284, "[设计改造·L2] 某页", [comment(10, "别人的提问")], assignees=("agent-2",))]
        out = self.run_sweep(issues)
        self.assertIn("超时 #284", out)
        self.assertIn("未按契约发 CLAIM", out)

    def test_open_pr_on_other_branch_does_not_exempt(self):
        issues = [
            issue(285, "[设计改造·L2] 另一页", [claim(200, "codex/285-slug"), heartbeat(120, "进行中")])
        ]
        out = self.run_sweep(issues, open_prs={"codex/unrelated-branch": 299})
        self.assertIn("超时 #285", out)


class TestStallClockStartsFromProgressComment(SweepTestCase):
    """停滞计时起点：只看 CLAIM/HEARTBEAT，别人的评论不能续命。"""

    def test_unrelated_comments_do_not_reset_the_clock(self):
        issues = [
            issue(
                286,
                "[设计改造·L2] 被评论续命的页",
                [
                    claim(200, "codex/286-slug"),
                    heartbeat(190, "进行中", note="在做"),
                    comment(30, "主 agent 的验收评论/别人的提问，不该重置计时"),
                ],
            )
        ]
        out = self.run_sweep(issues)
        self.assertIn("超时 #286", out)
        # 按旧逻辑（最后一条评论 30 分钟前）这条会被判正常。
        self.assertIn("距今 190 分钟", out)

    def test_recent_heartbeat_is_normal(self):
        issues = [issue(287, "[设计改造·L2] 活着的页", [claim(90, "codex/287-slug"), heartbeat(10, "进行中")])]
        out = self.run_sweep(issues)
        self.assertEqual(out.strip(), "巡检完成：扫描 1 个 design-impl issue，超时 0 个，待审查 0 个。")

    def test_threshold_is_configurable(self):
        issues = [issue(288, "[设计改造·L2] 阈值页", [claim(50, "codex/288-slug"), heartbeat(45, "进行中")])]
        self.assertIn("超时 #288", self.run_sweep(issues, timeout_minutes=30))
        self.assertNotIn("超时 #288", self.run_sweep(issues, timeout_minutes=60))


class TestDryRunCounting(SweepTestCase):
    """issue #281 次要问题：dry-run 的计数必须与逐条输出一致。"""

    def test_dry_run_counts_every_swept_issue(self):
        issues = [
            issue(289, "[设计改造·L2] 甲", [claim(200, "codex/289-a"), heartbeat(199, "进行中")]),
            issue(290, "[设计改造·L2] 乙", [claim(200, "codex/290-b"), heartbeat(120, "进行中")]),
            issue(291, "[设计改造·L2] 丙", [claim(800, "codex/291-c"), heartbeat(700, "待审查")]),
            issue(292, "[设计改造·L2] 丁", [claim(20, "codex/292-d"), heartbeat(5, "进行中")]),
        ]
        out = self.run_sweep(issues, dry_run=True)
        swept_lines = [line for line in out.splitlines() if line.startswith("超时 #")]
        self.assertEqual(len(swept_lines), 2)
        self.assertIn("扫描 4 个 design-impl issue，超时 2 个，待审查 1 个", out)

    def test_empty_queue_output(self):
        out = self.run_sweep([], dry_run=True)
        self.assertEqual(out.strip(), "巡检完成：扫描 0 个 design-impl issue，超时 0 个，待审查 0 个。")


if __name__ == "__main__":
    unittest.main()
