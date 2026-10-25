#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""check_lint.py 的纯函数单元测试（标准库 unittest，零第三方依赖）。

跑法：在仓库根目录执行
    python3 -m unittest discover -s scripts -p 'test_*.py' -v
"""
import unittest

from check_lint import count_errors, error_files, evaluate_lint


def report(messages_per_file):
    return [
        {"filePath": f"/repo/{name}", "messages": messages}
        for name, messages in messages_per_file
    ]


class TestCountErrors(unittest.TestCase):
    def test_only_severity_two_counts(self):
        data = report(
            [
                ("a.js", [{"severity": 2}, {"severity": 2}, {"severity": 1}]),
                ("b.js", [{"severity": 1}]),
            ]
        )
        self.assertEqual(count_errors(data), 2)

    def test_empty_report(self):
        self.assertEqual(count_errors([]), 0)


class TestEvaluateLint(unittest.TestCase):
    def test_first_run_freezes_current_value(self):
        ok, new_baseline, _ = evaluate_lint(174, None)
        self.assertTrue(ok)
        self.assertEqual(new_baseline, 174)

    def test_regression_fails(self):
        ok, new_baseline, note = evaluate_lint(180, 174)
        self.assertFalse(ok)
        self.assertEqual(new_baseline, 174)
        self.assertIn("涨到", note)

    def test_improvement_tightens_baseline(self):
        ok, new_baseline, _ = evaluate_lint(120, 174)
        self.assertTrue(ok)
        self.assertEqual(new_baseline, 120)

    def test_unchanged_passes_and_keeps_baseline(self):
        ok, new_baseline, _ = evaluate_lint(174, 174)
        self.assertTrue(ok)
        self.assertEqual(new_baseline, 174)


class TestErrorFiles(unittest.TestCase):
    def test_worst_files_first(self):
        data = report(
            [
                ("few.js", [{"severity": 2}]),
                ("many.js", [{"severity": 2}] * 5),
                ("clean.js", []),
            ]
        )
        self.assertEqual(error_files(data)[0], "/repo/many.js")


if __name__ == "__main__":
    unittest.main()
