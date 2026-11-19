#!/usr/bin/env python3
"""小程序响应判据静态检查。

后端 ResponseResult.code 是字符串 "200"，因此小程序里 `res.code === 200`（数字比较）
恒为假，等于把接口成功判成失败（见 #598）。本脚本把这类判据挡在合并之前，避免再退回。

允许的例外（见 ALLOWED，均带注释说明）：封装层对旧格式的兼容分支，以及 payment.js 对
响应格式的双形式适配（数字与字符串形式都认，不存在恒为假的问题）。
"""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "apps" / "miniapp" / "src"

# 数字形式的 code 比较，恒为假（带点号 `res.code === 200` 与裸变量 `code === 200` 都查）
# \bcode 同时覆盖带点号的 res.code 与裸变量 code 两种写法
PATTERN = re.compile(r"\bcode\s*(===|==)\s*200\b")

# 明确保留的例外：utils/request.js 是小程序最底层的响应归一化封装，
# 它的成功判定同时兼容 success=true / code=200 / code='200'，
# 其中 code=200（数字）是针对"旧格式或非标准响应"的防御分支，不属于业务判据。
ALLOWED = {
    # 封装层自身：成功判定同时兼容 success=true / code=200 / code='200'，
    # 数字形式是针对旧格式或非标准响应的防御分支，不是业务判据。
    "utils/request.js": "封装层对旧格式的兼容分支",
    # 该处把 code 提取成局部变量后，数字与字符串两种形式都认，属于刻意适配，恒为假的问题不存在。
    "api/modules/payment.js": "payment.js 对响应格式的双形式适配，见该处注释",
}


def main() -> int:
    if not SRC.is_dir():
        print(f"未找到 {SRC}")
        return 1

    violations = []
    scanned = 0
    for path in sorted(SRC.rglob("*")):
        if path.suffix not in (".vue", ".js"):
            continue
        relative = path.relative_to(SRC).as_posix()
        rel_for_allow = relative
        try:
            lines = path.read_text(encoding="utf-8").splitlines()
        except UnicodeDecodeError:
            continue
        scanned += 1
        allow = ALLOWED.get(rel_for_allow)
        for index, line in enumerate(lines, start=1):
            if not PATTERN.search(line):
                continue
            if allow:
                print(f"  例外保留 {relative}:{index} —— {allow}")
                continue
            violations.append(f"{relative}:{index}  {line.strip()}")

    if violations:
        print("小程序里仍存在恒为假的响应判据 res.code === 200：")
        for item in violations:
            print(f"  {item}")
        print("判据请改为 res.success === true（后端 success 字段才是权威信号）。")
        return 1

    print(f"小程序响应判据检查通过（扫描 {scanned} 个文件，数字形式 code 比较：0 处）")
    return 0


if __name__ == "__main__":
    sys.exit(main())
