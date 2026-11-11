#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""check_privacy.py 纯函数的单元测试（标准库 unittest，零第三方依赖）。

跑法：在仓库根目录执行
    python3 -m unittest discover -s scripts -p 'test_*.py' -v

重点覆盖两类判断：禁止路径是否只针对 uploads 目录段、密钥特征是否高置信度
且不回显明文；同时确认占位符与合成素材不会被误报。
"""
import unittest

from check_privacy import (
    SYNTHETIC_ASSET_DIR,
    forbidden_path_violations,
    secret_violations,
    _redact,
)


class ForbiddenPathTests(unittest.TestCase):
    def test_tracked_upload_dir_is_flagged(self):
        paths = [
            "apps/server/src/main/resources/static/uploads/3384650106421960/a.png",
            "uploads/chat/x.png",
        ]
        hits = forbidden_path_violations(paths)
        self.assertEqual(2, len(hits))
        self.assertTrue(all(h["kind"] == "forbidden_path" for h in hits))

    def test_synthetic_assets_are_allowed(self):
        paths = [SYNTHETIC_ASSET_DIR + "demo-user-avatar.png"]
        self.assertEqual([], forbidden_path_violations(paths))

    def test_filename_containing_upload_is_not_a_directory_match(self):
        # useImageUpload.js 只是文件名里带 upload，不是 uploads 目录段，不应误报。
        paths = ["apps/client/src/renderer/src/composables/useImageUpload.js"]
        self.assertEqual([], forbidden_path_violations(paths))


class SecretScanTests(unittest.TestCase):
    def test_aws_key_is_detected(self):
        sample = "AKIA" + "IOSFODNN7EXAMPL" + "E"  # 拼接避免本文件被自己扫到
        hits = secret_violations("config/x.yml", f"key: {sample}\n")
        self.assertEqual(1, len(hits))
        self.assertEqual("aws_access_key", hits[0]["pattern"])

    def test_private_key_block_is_detected(self):
        hits = secret_violations(
            "deploy/key.pem", "-----BEGIN RSA PRIVATE KEY-----\nMIIabc\n"
        )
        self.assertEqual(1, len(hits))
        self.assertEqual("private_key", hits[0]["pattern"])

    def test_excerpt_is_redacted(self):
        sample = "AKIA" + "IOSFODNN7EXAMPL" + "E"
        hits = secret_violations("config/x.yml", sample)
        self.assertNotIn(sample, hits[0]["excerpt"])

    def test_placeholders_and_env_injection_are_not_flagged(self):
        text = (
            "AI_API_KEY=\n"
            "JWT_SECRET=change_me_to_a_32_plus_char_random_string\n"
            "password: ${DB_PASSWORD}\n"
            "hash: $2a$10$HzKwbVHKIANfyd3QUJf3zu1ya0Kh2zOhzQL0JSnsTEQs6UgBkukBe\n"
        )
        self.assertEqual([], secret_violations("deploy/.env.example", text))

    def test_self_referential_rule_file_is_skipped(self):
        # 规则文件里写着 AKIA 之类的正则片段，扫描它本身没有意义。
        sample = "AKIA" + "IOSFODNN7EXAMPL" + "E"
        self.assertEqual([], secret_violations("scripts/check_privacy.py", sample))


class RedactTests(unittest.TestCase):
    def test_short_token_is_fully_masked(self):
        self.assertEqual("****", _redact("abcd"))

    def test_long_token_keeps_only_edges(self):
        self.assertEqual("abcd…wxyz", _redact("abcdMIDDLEwxyz"))


if __name__ == "__main__":
    unittest.main()
