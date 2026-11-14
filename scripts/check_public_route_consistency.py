#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""公开接口清单一致性检查（#576）。

`SecurityConfig` 改成默认拒绝后，「哪条接口匿名可调」由后端单方面决定。
前端两端各自维护一份白名单，用来判断「要不要带令牌」：

  - 桌面端 `apps/client/src/renderer/src/utils/authSession.js` 的 `PUBLIC_API_PATHS`
  - 小程序 `apps/miniapp/src/utils/request.js` 的 `AUTH_WHITELIST`

这两份清单一旦与后端 `permitAll` 漂移，失败形态**很难定位**：

  - 前端多写了（后端其实要登录）→ 前端不发令牌 → 401 → 全局拦截器判定
    「登录态失效」→ 清 localStorage 跳登录页。用户看到的是「打开页面被弹回登录」，
    而问题其实是白名单里一条拼错的路径。#576 就实际抓到过：
    小程序写 `/v1/admin/login`，后端真实路径是 `/admin/login`（多了一层 `v1`）。
  - 前端少写了（后端其实放行）→ 匿名请求带了令牌，通常无害，但多一次无谓的
    鉴权解析；且说明前端对公开面理解不全，容易在后端收紧时漏改。

因此本脚本做**单向硬约束**：前端声明的每一条都必须能被后端 `permitAll` 覆盖。
反向（后端放行但前端没写）不报错，因为两端职责不同——桌面端不加载小程序
tabBar 页的 `/v1/recipe/**`，小程序不渲染 `/uploads/**` 的 `<img>`，
这些「后端公开但某端不涉及」的差异是正常的，硬报会产生高频误报而被忽略。

零第三方依赖。用法见 main()。
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
DESKTOP_AUTH_SESSION = (
    REPO_ROOT / "apps/client/src/renderer/src/utils/authSession.js"
)
MINIAPP_REQUEST = REPO_ROOT / "apps/miniapp/src/utils/request.js"

# 复用 gen_route_auth_matrix 的路由语义与 permitAll 解析，避免两处逻辑分叉：
# 若各自实现一套，其中一处修了 bug 另一处没修，校验会静默失效。
sys.path.insert(0, str(REPO_ROOT / "scripts"))
from gen_route_auth_matrix import (  # noqa: E402
    normalize,
    pattern_covers,
    public_patterns,
    to_segments,
)


def client_paths(path: Path, block_start_re: str, block_end_re: str) -> list[str]:
    """抽出某个白名单数组里的路径字面量。

    只取 `block_start_re` 与 `block_end_re` 之间的内容，避免把同文件里
    其他数组（如 API 路径常量、路由跳转串）误算进来。
    `block_end_re` 里的 `^` 需按行首匹配，故两个正则都开 MULTILINE——
    否则块结束标记匹配不到，会把文件剩余部分整段吞掉（曾实测踩到）。
    """
    text = path.read_text(encoding="utf-8")
    start_match = re.search(block_start_re, text, re.MULTILINE)
    if not start_match:
        raise SystemExit(f"未找到白名单数组起始标记：{path}")
    rest = text[start_match.end() :]
    end_match = re.search(block_end_re, rest, re.MULTILINE)
    block = rest[: end_match.start()] if end_match else rest
    return re.findall(r"['\"](/[^'\"]*)['\"]", block)


def miniapp_whitelist() -> list[str]:
    return client_paths(
        MINIAPP_REQUEST,
        r"const AUTH_WHITELIST\s*=\s*\[",
        r"^\s*\]",
    )


def desktop_public_paths() -> list[str]:
    return client_paths(
        DESKTOP_AUTH_SESSION,
        r"const PUBLIC_API_PATHS\s*=\s*\[",
        r"^\s*\]",
    )


def covered_by_backend(client_path: str, patterns: list[tuple[str | None, str]]) -> bool:
    """客户端路径是否被后端某个 permitAll 模式覆盖。

    客户端条目可能比后端模式更具体（`/v1/captcha/checkCode` vs `/v1/captcha/**`），
    也可能完全一致。判据是把「客户端路径当作一条具体路由」，看是否有模式覆盖它。
    """
    route_segments = to_segments(normalize(client_path))
    for http_method, pattern in patterns:
        # 客户端白名单不区分方法（它只决定「带不带令牌」），因此任一方法覆盖即可。
        if pattern_covers(http_method, pattern, "GET", client_path):
            return True
        if pattern_covers(http_method, pattern, "POST", client_path):
            return True
    # 落到这里说明没有被覆盖；再用一个宽松的「前缀通配」兜底，
    # 处理 `/v1/captcha/**` 这类模式对 `/v1/captcha`（不带尾段）的覆盖。
    for _http_method, pattern in patterns:
        if not pattern.endswith("/**"):
            continue
        head = pattern[: -len("/**")]
        head_segments = to_segments(normalize(head))
        if len(route_segments) >= len(head_segments) and all(
            ps in ("*", "**") or ps == rs
            for ps, rs in zip(head_segments, route_segments)
        ):
            return True
    return False


def find_violations() -> list[dict]:
    patterns = public_patterns()
    violations: list[dict] = []
    for client in ("desktop", "miniapp"):
        paths = desktop_public_paths() if client == "desktop" else miniapp_whitelist()
        for path in paths:
            if not covered_by_backend(path, patterns):
                violations.append({"client": client, "path": path})
    return violations


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description="公开接口清单一致性检查")
    parser.add_argument("--format", choices=["human", "json"], default="human")
    args = parser.parse_args(argv)

    patterns = public_patterns()
    violations = find_violations()

    if args.format == "json":
        print(json.dumps(
            {"backend_patterns": len(patterns), "violations": violations},
            ensure_ascii=False, indent=2,
        ))
    else:
        print(f"后端 permitAll 模式: {len(patterns)} 条")
        print(f"桌面端白名单: {len(desktop_public_paths())} 条")
        print(f"小程序白名单: {len(miniapp_whitelist())} 条")
        if not violations:
            print("未发现「前端声明公开但后端不放行」的路径")
        else:
            print(f"\n发现 {len(violations)} 条漂移（前端会不带令牌请求，必然 401）：")
            for v in violations:
                print(f"  [{v['client']}] {v['path']}")

    return 1 if violations else 0


if __name__ == "__main__":
    sys.exit(main())
