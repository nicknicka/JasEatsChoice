#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""check_h5_build.py 纯函数的单元测试（标准库 unittest，零第三方依赖）。

跑法：在仓库根目录执行
    python3 -m unittest discover -s scripts -p 'test_*.py' -v

覆盖三条判据：产物缺失、体积不足（空壳产物）、没有页面级 chunk；
并确认完整产物不会被误报。
"""
import tempfile
import unittest
from pathlib import Path

from check_h5_build import (
    DEFAULT_MIN_BYTES,
    DEFAULT_MIN_PAGE_CHUNKS,
    collect_metrics,
    evaluate,
)


def _make_dist(tmp: str, *, index_html=True, payload=b"x" * 100, page_chunks=1) -> Path:
    """在临时目录里造一个（可能是空壳的）H5 产物。"""
    dist = Path(tmp) / "h5"
    assets = dist / "assets"
    assets.mkdir(parents=True)
    if index_html:
        (dist / "index.html").write_text("<html></html>", encoding="utf-8")
    (assets / "index-abc.js").write_bytes(payload)
    for i in range(page_chunks):
        (assets / f"pages-user-review-submit-index-{i}.js").write_bytes(payload)
    return dist


class CollectMetricsTests(unittest.TestCase):
    def test_missing_dist_yields_empty_metrics(self):
        with tempfile.TemporaryDirectory() as tmp:
            metrics = collect_metrics(Path(tmp) / "nope")
            self.assertFalse(metrics["exists"])
            self.assertEqual(0, metrics["total_bytes"])
            self.assertEqual([], metrics["page_chunks"])

    def test_counts_bytes_files_and_page_chunks(self):
        with tempfile.TemporaryDirectory() as tmp:
            dist = _make_dist(tmp, payload=b"x" * 50, page_chunks=3)
            metrics = collect_metrics(dist)
            self.assertTrue(metrics["exists"])
            self.assertTrue(metrics["has_index_html"])
            # index.html(13) + index-abc.js(50) + 3 个页面 chunk(各 50)
            self.assertEqual(213, metrics["total_bytes"])
            self.assertEqual(5, metrics["file_count"])
            self.assertEqual(3, len(metrics["page_chunks"]))


class EvaluateTests(unittest.TestCase):
    def test_missing_dist_fails(self):
        violations = evaluate(
            {"dist": "/x", "exists": False, "total_bytes": 0, "file_count": 0,
             "page_chunks": [], "has_index_html": False},
            DEFAULT_MIN_BYTES, DEFAULT_MIN_PAGE_CHUNKS,
        )
        self.assertEqual(["missing_dist"], [v["kind"] for v in violations])

    def test_empty_shell_artifact_is_flagged(self):
        # 复现 #583 的失败形态：12KB 级别、零页面 chunk。
        metrics = {
            "dist": "/x", "exists": True, "total_bytes": 12465 + 7221,
            "file_count": 2, "page_chunks": [], "has_index_html": True,
        }
        kinds = [v["kind"] for v in evaluate(metrics, DEFAULT_MIN_BYTES, DEFAULT_MIN_PAGE_CHUNKS)]
        self.assertIn("too_small", kinds)
        self.assertIn("no_page_chunks", kinds)

    def test_missing_index_html_is_flagged(self):
        metrics = {
            "dist": "/x", "exists": True, "total_bytes": 10 * 1024 * 1024,
            "file_count": 1, "page_chunks": ["pages-a.js"], "has_index_html": False,
        }
        kinds = [v["kind"] for v in evaluate(metrics, DEFAULT_MIN_BYTES, DEFAULT_MIN_PAGE_CHUNKS)]
        self.assertEqual(["missing_index_html"], kinds)

    def test_complete_artifact_passes(self):
        with tempfile.TemporaryDirectory() as tmp:
            dist = _make_dist(tmp, payload=b"x" * 600_000, page_chunks=80)
            violations = evaluate(
                collect_metrics(dist), DEFAULT_MIN_BYTES, DEFAULT_MIN_PAGE_CHUNKS
            )
            self.assertEqual([], violations)

    def test_thresholds_are_overridable(self):
        metrics = {
            "dist": "/x", "exists": True, "total_bytes": 1000,
            "file_count": 1, "page_chunks": ["pages-a.js"], "has_index_html": True,
        }
        self.assertEqual([], evaluate(metrics, min_bytes=500, min_page_chunks=1))


if __name__ == "__main__":
    unittest.main()
