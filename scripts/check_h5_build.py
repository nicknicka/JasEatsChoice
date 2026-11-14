#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""小程序 H5 产物完整性门禁（#583）。

为什么需要它：`apps/miniapp` 的 `dev:h5` / `build:h5` 曾长期**静默失效**。
`package.json` 没声明 `@dcloudio/uni-h5` 与 `@dcloudio/uni-h5-vite` 时，
uni 的平台插件是按**项目依赖列表**去 require 的（不是目录探测），
列表里没有 → H5 平台插件不注册 → `src/main.js` 的 `createApp()` 从未被调用。

失败形态极其隐蔽：`npm run build:h5` **退出码 0、无任何警告**，
但产物只有约 12 KB（只有 pinia，没有应用入口），dev 模式则白屏且控制台无报错。
构建「成功」与构建「可用」之间的这条缝，靠肉眼很难察觉。

这个脚本守住的判据是产物本身——一旦产物退回空壳就当场失败：

  1. `index.html` 必须存在；
  2. 产物总字节数不低于 `--min-bytes`（默认 500 KiB）；
  3. 页面级 chunk（`assets/pages-*.js`）数量不低于 `--min-page-chunks`（默认 1）。

第 3 条是比体积更本质的信号：空壳产物的特征正是**没有任何页面 chunk**，
说明应用入口压根没被编译进来，而不是「页面变少了」。

用法：
    python3 scripts/check_h5_build.py
    python3 scripts/check_h5_build.py --dist apps/miniapp/dist/build/h5 --min-bytes 512000

零第三方依赖。
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_DIST = REPO_ROOT / "apps/miniapp/dist/build/h5"

# 空壳产物约 12–19 KB，完整产物约 2 MB，阈值取 500 KiB 留足余量：
# 既不会被页面正常增减误报，又能在「应用入口整个丢失」时立刻失败。
DEFAULT_MIN_BYTES = 500 * 1024
DEFAULT_MIN_PAGE_CHUNKS = 1

# 页面级 chunk 的命名：uni 会把 `src/pages-user/review/submit/index.vue`
# 编成 `pages-user-review-submit-index.<hash>.js`。
PAGE_CHUNK_RE = re.compile(r"^pages-.*\.js$")


def collect_metrics(dist: Path) -> dict:
    """统计产物指标。

    `dist` 不存在（没构建过）不是异常，而是「指标全空」——由 evaluate 判失败，
    这样门禁在 CI 里首次跑也能给出清晰结论，而不是抛栈。
    """
    if not dist.is_dir():
        return {
            "dist": str(dist),
            "exists": False,
            "total_bytes": 0,
            "file_count": 0,
            "page_chunks": [],
            "has_index_html": False,
        }

    total_bytes = 0
    file_count = 0
    for p in dist.rglob("*"):
        if p.is_file():
            file_count += 1
            total_bytes += p.stat().st_size

    assets = dist / "assets"
    page_chunks = sorted(
        p.name for p in assets.iterdir() if p.is_file() and PAGE_CHUNK_RE.match(p.name)
    ) if assets.is_dir() else []

    return {
        "dist": str(dist),
        "exists": True,
        "total_bytes": total_bytes,
        "file_count": file_count,
        "page_chunks": page_chunks,
        "has_index_html": (dist / "index.html").is_file(),
    }


def evaluate(metrics: dict, min_bytes: int, min_page_chunks: int) -> list[dict]:
    """唯一的 seam：产物指标 -> 违规清单（空列表 = 门禁通过）。"""
    violations: list[dict] = []

    if not metrics["exists"]:
        violations.append({
            "kind": "missing_dist",
            "detail": f"产物目录不存在：{metrics['dist']}（先跑 npm run build:h5）",
        })
        return violations

    if not metrics["has_index_html"]:
        violations.append({"kind": "missing_index_html", "detail": "缺少 index.html"})

    if metrics["total_bytes"] < min_bytes:
        violations.append({
            "kind": "too_small",
            "detail": (
                f"产物仅 {metrics['total_bytes']} 字节，低于阈值 {min_bytes} 字节"
                "——疑似应用入口未编译进来（H5 平台插件未注册）"
            ),
        })

    page_chunk_count = len(metrics["page_chunks"])
    if page_chunk_count < min_page_chunks:
        violations.append({
            "kind": "no_page_chunks",
            "detail": (
                f"页面级 chunk 仅 {page_chunk_count} 个，低于阈值 {min_page_chunks}"
                "——空壳产物的典型特征"
            ),
        })

    return violations


def main(argv: "list[str] | None" = None) -> int:
    parser = argparse.ArgumentParser(description="小程序 H5 产物完整性门禁（#583）")
    parser.add_argument("--dist", default=str(DEFAULT_DIST), help="H5 产物目录")
    parser.add_argument("--min-bytes", type=int, default=DEFAULT_MIN_BYTES,
                        help=f"产物总字节下限（默认 {DEFAULT_MIN_BYTES}）")
    parser.add_argument("--min-page-chunks", type=int, default=DEFAULT_MIN_PAGE_CHUNKS,
                        help=f"页面级 chunk 数下限（默认 {DEFAULT_MIN_PAGE_CHUNKS}）")
    parser.add_argument("--format", choices=["human", "json"], default="human")
    args = parser.parse_args(argv)

    metrics = collect_metrics(Path(args.dist))
    violations = evaluate(metrics, args.min_bytes, args.min_page_chunks)

    if args.format == "json":
        print(json.dumps({"metrics": metrics, "violations": violations},
                         ensure_ascii=False, indent=2))
    else:
        if metrics["exists"]:
            print(
                f"H5 产物：{metrics['file_count']} 个文件 / "
                f"{metrics['total_bytes']} 字节 / "
                f"{len(metrics['page_chunks'])} 个页面级 chunk"
            )
        if not violations:
            print("产物完整")
        else:
            print(f"发现 {len(violations)} 处问题：")
            for v in violations:
                print(f"  [{v['kind']}] {v['detail']}")

    return 1 if violations else 0


if __name__ == "__main__":
    sys.exit(main())
