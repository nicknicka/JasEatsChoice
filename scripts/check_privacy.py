#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""隐私守卫：禁止把运行时上传文件和真实密钥带进 Git。

背景：用户反馈闭环会把真实反馈正文、附件、联系方式、订单/支付标识、聊天与
模型上下文带进仓库附近。这些数据只允许存在于仓库外的受控运行时存储里；一旦
被 `git add` 进版本库，就再也收不回来（已推送的提交无法真正撤回）。

这个脚本守两条底线：

  1. **禁止路径**：任何被 Git 跟踪的 `uploads/` 目录（含
     `apps/server/src/main/resources/static/uploads/`）都视为运行时上传数据。
     合成素材必须放到明确的合成目录，不能用反选规则把上传目录重新纳入跟踪。
  2. **密钥模式**：跟踪的文本文件里出现高置信度的私钥 / 云厂商 / 平台令牌时失败。

判据刻意保持高置信度、低误报：宁可漏掉模糊的可疑串，也不要让一个天天误报的
门禁被绕过。凭据正确做法仍是只写 `deploy/.env`（gitignore）与 CI Secrets。

零第三方依赖。用法见 main()。
"""
from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent

# 允许存在合成素材的唯一位置（见 static/synthetic-assets/README.md）。
SYNTHETIC_ASSET_DIR = "apps/server/src/main/resources/static/synthetic-assets/"

# 跟踪路径里出现这些目录段即违规：运行时上传目录可能出现在仓库任意层级。
FORBIDDEN_PATH_SEGMENTS = ("uploads",)

# 高置信度密钥特征。每一项都是「这一串本身就是凭据」，不做模糊的 key=value 猜测，
# 避免把占位符、bcrypt 哈希、示例值统统报成泄漏。
SECRET_PATTERNS: tuple[tuple[str, re.Pattern[str]], ...] = (
    ("private_key", re.compile(r"-----BEGIN [A-Z ]*PRIVATE KEY-----")),
    ("aws_access_key", re.compile(r"\bAKIA[0-9A-Z]{16}\b")),
    ("github_token", re.compile(r"\bgh[pousr]_[A-Za-z0-9]{36,}\b")),
    ("openai_key", re.compile(r"\bsk-[A-Za-z0-9_-]{32,}\b")),
    ("slack_token", re.compile(r"\bxox[baprs]-[A-Za-z0-9-]{10,}\b")),
    ("google_api_key", re.compile(r"\bAIza[0-9A-Za-z_-]{35}\b")),
)

# 这些文件里出现的示例串是「描述检测规则」用的，不是真实凭据，扫描时跳过内容。
SELF_REFERENTIAL_FILES = {
    "scripts/check_privacy.py",
    "scripts/test_check_privacy.py",
}

# 跳过明显二进制 / 生成物，避免把图片字节当文本扫。
BINARY_SUFFIXES = {
    ".png", ".jpg", ".jpeg", ".gif", ".webp", ".ico", ".bmp",
    ".pdf", ".zip", ".gz", ".tgz", ".jar", ".woff", ".woff2", ".ttf",
    ".mp3", ".mp4", ".mov", ".xlsx", ".docx", ".pptx", ".dump",
}


def tracked_paths(root: Path) -> list[str]:
    """返回仓库跟踪的相对路径（POSIX 分隔符）。"""
    out = subprocess.run(
        ["git", "-C", str(root), "ls-files", "-z"],
        check=True, capture_output=True,
    ).stdout
    return [p.decode("utf-8") for p in out.split(b"\x00") if p]


def forbidden_path_violations(paths: list[str]) -> list[dict]:
    """落库判据：跟踪路径里出现禁止目录段。合成素材目录除外。"""
    violations: list[dict] = []
    for path in paths:
        if path.startswith(SYNTHETIC_ASSET_DIR):
            continue
        segments = path.split("/")
        for segment in FORBIDDEN_PATH_SEGMENTS:
            if segment in segments:
                violations.append({"kind": "forbidden_path", "path": path, "reason": segment})
                break
    return violations


def secret_violations(path: str, text: str) -> list[dict]:
    """在单个文本文件里找高置信度密钥，返回带行号的命中。"""
    if path in SELF_REFERENTIAL_FILES:
        return []
    found: list[dict] = []
    for lineno, line in enumerate(text.splitlines(), start=1):
        for name, pattern in SECRET_PATTERNS:
            match = pattern.search(line)
            if match:
                found.append({
                    "kind": "secret",
                    "path": path,
                    "line": lineno,
                    "pattern": name,
                    "excerpt": _redact(match.group(0)),
                })
    return found


def _redact(token: str) -> str:
    """只保留少量头尾字符，避免把命中串再次写进日志/CI 输出。"""
    if len(token) <= 8:
        return "*" * len(token)
    return f"{token[:4]}…{token[-4:]}"


def looks_binary(path: str) -> bool:
    return Path(path).suffix.lower() in BINARY_SUFFIXES


def collect_violations(root: Path) -> list[dict]:
    paths = tracked_paths(root)
    violations = forbidden_path_violations(paths)
    for path in paths:
        if looks_binary(path):
            continue
        full = root / path
        try:
            text = full.read_text(encoding="utf-8")
        except (UnicodeDecodeError, OSError):
            continue
        violations.extend(secret_violations(path, text))
    return violations


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description="隐私守卫：禁止上传目录与密钥入库")
    parser.add_argument("--format", choices=["human", "json"], default="human")
    parser.add_argument("--root", default=str(REPO_ROOT), help="仓库根目录（测试用）")
    args = parser.parse_args(argv)

    root = Path(args.root)
    try:
        violations = collect_violations(root)
    except subprocess.CalledProcessError as exc:
        print(f"无法读取 git 跟踪文件：{exc}", file=sys.stderr)
        return 2

    if args.format == "json":
        print(json.dumps({"violations": violations}, ensure_ascii=False, indent=2))
    else:
        if not violations:
            print("未发现被跟踪的上传目录或高置信度密钥")
        else:
            print(f"发现 {len(violations)} 处隐私违规：")
            for v in violations:
                if v["kind"] == "forbidden_path":
                    print(f"  [禁止路径] {v['path']}（命中目录段 {v['reason']}）")
                else:
                    print(f"  [密钥] {v['path']}:{v['line']} {v['pattern']} {v['excerpt']}")

    return 1 if violations else 0


if __name__ == "__main__":
    sys.exit(main())
