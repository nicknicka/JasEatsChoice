#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""check_public_route_consistency.py 的单元测试（标准库 unittest，零第三方依赖）。

固定两类曾经踩到的错：

  1. **抽取边界**：白名单数组的结束标记 `^\\s*\\]` 若不加 MULTILINE，
     匹配不到行首 `]`，会把文件剩余部分整段吞进来——于是路由跳转串
     （`/pages/login/index?role=merchant`）、*orders* 之类的日志串
     全被当成白名单条目，误报一片。这里用一个「数组后面还有别的路径串」
     的临时文件锁住该行为。
  2. **拼错即漂移**：`/v1/admin/login`（后端真实路径是 `/admin/login`）
     必须被判为漂移。这正是 #576 实际发现的缺陷。

跑法：在仓库根目录执行
    python3 -m unittest discover -s scripts -p 'test_*.py' -v
"""
import tempfile
import unittest
from pathlib import Path

from check_public_route_consistency import (
    client_paths,
    covered_by_backend,
    public_patterns,
)


class TestClientPathsExtraction(unittest.TestCase):
    def test_stops_at_block_end_without_swallowing_rest(self):
        text = (
            "const AUTH_WHITELIST = [\n"
            "  '/v1/users/login',   // 用户登录\n"
            "  '/v1/captcha'\n"
            "]\n"
            "\n"
            "// 下面这些是路由跳转串，不是白名单\n"
            "const LOGIN_PAGE = '/pages/login/index'\n"
            "if (url.includes('/orders')) { /* 日志 */ }\n"
        )
        with tempfile.TemporaryDirectory() as tmp:
            f = Path(tmp) / "request.js"
            f.write_text(text, encoding="utf-8")
            paths = client_paths(
                f, r"const AUTH_WHITELIST\s*=\s*\[", r"^\s*\]"
            )
        self.assertEqual(paths, ["/v1/users/login", "/v1/captcha"])

    def test_single_line_block(self):
        text = "const PUBLIC_API_PATHS = [ '/admin/login' ]\n"
        with tempfile.TemporaryDirectory() as tmp:
            f = Path(tmp) / "authSession.js"
            f.write_text(text, encoding="utf-8")
            paths = client_paths(
                f, r"const PUBLIC_API_PATHS\s*=\s*\[", r"^\s*\]"
            )
        self.assertEqual(paths, ["/admin/login"])


class TestCoveredByBackend(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.patterns = public_patterns()

    def test_exact_path_is_covered(self):
        self.assertTrue(covered_by_backend("/v1/users/login", self.patterns))

    def test_wildcard_pattern_covers_specific_path(self):
        # 后端登记的是 /v1/captcha/**，客户端写更具体的 /v1/captcha/checkCode
        self.assertTrue(covered_by_backend("/v1/captcha/checkCode", self.patterns))

    def test_bare_prefix_covered_by_double_star(self):
        # 客户端写 /v1/captcha（不带尾段），后端是 /v1/captcha/**
        self.assertTrue(covered_by_backend("/v1/captcha", self.patterns))

    def test_extra_v1_segment_is_a_violation(self):
        # #576 实测缺陷：小程序误写 /v1/admin/login，后端真实路径 /admin/login
        self.assertFalse(covered_by_backend("/v1/admin/login", self.patterns))

    def test_admin_login_is_covered(self):
        self.assertTrue(covered_by_backend("/admin/login", self.patterns))

    def test_unknown_path_is_a_violation(self):
        self.assertFalse(covered_by_backend("/v1/wallet/info/1", self.patterns))


if __name__ == "__main__":
    unittest.main()
