#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""ESLint 门禁：把当前 error 数与冻结基线比较。

仓库的 lint 现在是红的（几百处存量错误），一次性清零不现实，所以这里不追求零错误，
而是**冻结住不许变差**：只能持平或下降，涨一格就判失败。下降时把基线同步下调，
把改进锁住，防止又涨回去。

零第三方依赖。输入是 `eslint -f json` 的输出，所以 lint 本身怎么跑是它的事。
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_BASELINE = REPO_ROOT / "scripts/baselines/lint-baseline.txt"


def count_errors(report: "list[dict]") -> int:
    """统计 error 级问题数。severity 2 = error，1 = warning（warning 不进基线）。"""
    return sum(
        1
        for file_report in report
        for msg in file_report.get("messages", [])
        if msg.get("severity") == 2
    )


def error_files(report: "list[dict]", limit: int = 10) -> "list[str]":
    """报出 error 最多的几个文件，省得人自己去 json 里翻。"""
    counts = [
        (len([m for m in f.get("messages", []) if m.get("severity") == 2]), f.get("filePath", ""))
        for f in report
    ]
    return [path for count, path in sorted(counts, reverse=True)[:limit] if count]


def evaluate_lint(current: int, baseline: int) -> tuple:
    """唯一的 seam：(当前 error 数, 基线) -> (是否通过, 应写入的基线, 说明)。

    规则：涨了失败；降了就把基线也降下来（把改进固化）；相等就什么都不动。
    """
    if baseline is None:
        return True, current, f"首次运行，冻结基线 {current}"
    if current > baseline:
        return False, baseline, f"error 数从 {baseline} 涨到 {current}（+{current - baseline}）"
    if current < baseline:
        return True, current, f"error 数从 {baseline} 降到 {current}，基线已同步下调"
    return True, baseline, f"error 数持平于基线 {baseline}"


def read_baseline(path: Path) -> "int | None":
    if not path.exists():
        return None
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#"):
            return int(line)
    return None


def write_baseline(value: int, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        "\n".join(
            [
                "# ESLint error 级问题数的冻结基线。",
                "# 规则：只能持平或下降；下降时 `make check-lint` 会自动把这里改低并把改进锁住。",
                "# 不想让某类错误继续存在就修掉它，别改这个文件往上调。",
                "",
                str(value),
                "",
            ]
        ),
        encoding="utf-8",
    )


def main(argv: "list[str] | None" = None) -> int:
    parser = argparse.ArgumentParser(description="ESLint error 数与冻结基线比较")
    parser.add_argument("--json", required=True, help="eslint -f json 输出的文件")
    parser.add_argument("--baseline", default=str(DEFAULT_BASELINE))
    parser.add_argument("--update-baseline", action="store_true", help="把当前 error 数直接写成新基线")
    args = parser.parse_args(argv)

    report = json.loads(Path(args.json).read_text(encoding="utf-8"))
    current = count_errors(report)
    baseline_path = Path(args.baseline)

    if args.update_baseline:
        write_baseline(current, baseline_path)
        print(f"基线已更新：{current}")
        return 0

    baseline = read_baseline(baseline_path)
    ok, next_baseline, note = evaluate_lint(current, baseline)
    print(note)

    if ok and next_baseline != baseline:
        write_baseline(next_baseline, baseline_path)

    if not ok:
        print("error 最多的几个文件（不是全部，改完再跑一次看总数）：")
        for path in error_files(report):
            print(f"  {path}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
