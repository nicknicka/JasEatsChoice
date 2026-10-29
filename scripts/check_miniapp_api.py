#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""小程序 API 调用一致性检查。

小程序把接口按业务域拆在 apps/miniapp/src/api/modules/ 下，每个模块导出一个
`xxxApi` 对象。页面与 composable 通过 `xxxApi.method(...)` 调用它们。

问题在于：**调了一个模块里没有的方法，没有任何编译期或运行前检查能发现**。
它和「路径写错」不同——路径错会打到后端返回 404，容易在联调时暴露；而方法不
存在会在调用瞬间抛 `TypeError`，若外面套了 try/catch，用户只看到「点了没反应」，
日志里只有一行 console.error。这类缺陷因此能长期潜伏。

这个脚本做的事：把每个 api 模块**实际导出的方法名**抽出来，再扫出所有
`xxxApi.method(` 形式的调用点，比对两边，报出「调用了不存在的方法」。

## 为什么没有同时检查「未使用的源码文件」

本规格清理死文件时还用了另一个维度：某个 .vue/.js 是否被任何入口引用。
那个维度**不适合做成会失败的静态检查**，原因是在小程序里「被使用」有多种形式，
静态分析难以判全，误报率高：

  - **easycom 自动注册**：pages.json 的 easycom.custom 里配置的组件
    （如 `^AppImage$`）不需要显式 import 就会被使用；
  - **编译产物命名**：App.vue 编译后是 app.js（小写），用「产物里有没有
    同名文件」做判据会误判；
  - **动态路由**：navigateTo/redirectTo 的目标常由常量或模板串拼出，
    文本匹配容易漏。

这些都是实测踩到过的坑。一个高频误报的检查会很快被绕过或忽略，
反而失去价值——所以死文件清理做一次性人工核对（引用闭包 + 构建产物双重
校验 + 逐个复核）更为合适，而本脚本只做「API 方法一致性」这一条
判据明确、误报率低的检查。

零第三方依赖。用法见 main()。
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
MINIAPP_SRC = REPO_ROOT / "apps/miniapp/src"
API_MODULES = MINIAPP_SRC / "api/modules"

# JS 内建方法：它们可能出现在 api 对象名的位置（如 `api.map(...)`），不应算 API 调用。
JS_BUILTINS = {
    "map", "filter", "find", "forEach", "reduce", "reduceRight", "some", "every",
    "findIndex", "flat", "flatMap", "sort", "reverse", "slice", "splice", "concat",
    "join", "push", "pop", "shift", "unshift", "indexOf", "lastIndexOf", "includes",
    "fill", "copyWithin", "keys", "values", "entries", "at",
    "trim", "trimStart", "trimEnd", "toLowerCase", "toUpperCase", "substring",
    "substr", "charAt", "charCodeAt", "codePointAt", "padStart", "padEnd", "repeat",
    "split", "replace", "replaceAll", "match", "matchAll", "search", "startsWith",
    "endsWith", "normalize", "localeCompare", "toString", "valueOf",
    "then", "catch", "finally", "hasOwnProperty",
}


def strip_noise(src: str) -> str:
    """跳过注释 / 字符串 / 模板串 / 正则字面量，其余原样保留。

    为什么不能用简单的正则替换：注释里可能出现反引号（ai.js 的说明文字里就有
    ```json），正则字面量里也可能出现反引号。若按反引号配对去删，会把后续大段
    代码一起吞掉——那是静默的错误结果，比漏报更危险。
    """
    out: list[str] = []
    i, n = 0, len(src)
    last = ""  # 上一个有意义字符，用于判断 `/` 是除号还是正则起始
    while i < n:
        c = src[i]
        # 行注释
        if src.startswith("//", i):
            while i < n and src[i] != "\n":
                i += 1
            out.append(" ")
            continue
        # 块注释
        if src.startswith("/*", i):
            j = src.find("*/", i + 2)
            i = n if j < 0 else j + 2
            out.append(" ")
            continue
        # 单/双引号字符串
        if c in ('"', "'"):
            i += 1
            while i < n:
                if src[i] == "\\":
                    i += 2
                    continue
                if src[i] == c:
                    i += 1
                    break
                i += 1
            out.append(" ")
            last = "s"
            continue
        # 模板串（允许 ${} 内嵌表达式）
        if c == "`":
            i += 1
            depth = 0
            while i < n:
                if src[i] == "\\":
                    i += 2
                    continue
                if src[i] == "`" and depth == 0:
                    i += 1
                    break
                if src.startswith("${", i):
                    depth += 1
                    i += 2
                    continue
                if src[i] == "}" and depth > 0:
                    depth -= 1
                i += 1
            out.append(" ")
            last = "s"
            continue
        # 正则字面量：`/` 出现在表达式起始位置
        if c == "/" and last not in (")", "]", "w"):
            j = i + 1
            in_class = False
            while j < n:
                ch = src[j]
                if ch == "\\":
                    j += 2
                    continue
                if ch == "[":
                    in_class = True
                elif ch == "]":
                    in_class = False
                elif ch == "/" and not in_class:
                    j += 1
                    break
                elif ch == "\n":
                    break
                j += 1
            while j < n and src[j].isalpha():
                j += 1
            out.append(" ")
            i = j
            last = "s"
            continue
        out.append(c)
        if not c.isspace():
            last = "w" if (c.isalnum() or c in "_$") else c
        i += 1
    return "".join(out)


def object_keys(src: str, brace_start: int) -> set[str]:
    """取对象字面量第一层的键名。

    只在「对象属性位置」取键：花括号深度 1、圆括号与方括号深度 0，且上一个有意
    义字符是 `{` 或 `,`。这样能排除：
      - `AI_API.CHAT` 里的 CHAT（前面是 `.`）
      - `async (data, onMessage) => ...` 里的参数名（在圆括号内）
    """
    depth = paren = brack = 0
    prev_sig = ""
    keys: set[str] = set()
    i = brace_start
    while i < len(src):
        c = src[i]
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                break
        elif c == "(":
            paren += 1
        elif c == ")":
            paren -= 1
        elif c == "[":
            brack += 1
        elif c == "]":
            brack -= 1
        elif depth == 1 and paren == 0 and brack == 0 and prev_sig in ("{", ","):
            m = re.match(r"([A-Za-z_$][\w$]*)\s*(?=[:\(,])", src[i:])
            if m:
                keys.add(m.group(1))
                i += len(m.group(1))
                prev_sig = "k"
                continue
        if not c.isspace():
            prev_sig = c
        i += 1
    return keys


def collect_api_methods(modules_dir: Path = API_MODULES) -> dict[str, set[str]]:
    """抽出每个 api 对象实际导出的方法名。"""
    apis: dict[str, set[str]] = {}
    for f in sorted(modules_dir.glob("*.js")):
        src = strip_noise(f.read_text(encoding="utf-8", errors="ignore"))
        for m in re.finditer(r"export\s+const\s+([A-Za-z_$][\w$]*)\s*=\s*\{", src):
            brace = src.index("{", m.start())
            apis.setdefault(m.group(1), set()).update(object_keys(src, brace))
    return apis


def iter_source_files(root: Path = MINIAPP_SRC):
    for p in sorted(root.rglob("*")):
        if p.is_file() and p.suffix in (".vue", ".js"):
            # api 模块自身不算调用点
            if "api/modules" in p.as_posix():
                continue
            yield p


def collect_calls(files, api_names: set[str]) -> list[tuple[str, int, str, str]]:
    """扫出 `xxxApi.method(` 形式的调用点。

    只认同一个模块里出现过名字的 api 对象，避免把无关变量误判。
    返回的路径：在仓库内则相对仓库根，否则用原样路径（便于测试与外部目录）。
    """
    pattern = re.compile(r"\b(" + "|".join(sorted(api_names)) + r")\.([A-Za-z_$][\w$]*)\s*\(")
    calls: list[tuple[str, int, str, str]] = []
    for p in files:
        text = p.read_text(encoding="utf-8", errors="ignore")
        clean = strip_noise(text)
        try:
            shown = p.resolve().relative_to(REPO_ROOT.resolve()).as_posix()
        except ValueError:
            shown = p.as_posix()
        for m in pattern.finditer(clean):
            api, method = m.group(1), m.group(2)
            line = clean[: m.start()].count("\n") + 1
            calls.append((shown, line, api, method))
    return calls


def find_violations(apis: dict[str, set[str]], calls) -> list[dict]:
    out = []
    for path, line, api, method in calls:
        if method in JS_BUILTINS:
            continue
        if method not in apis.get(api, set()):
            out.append({"file": path, "line": line, "call": f"{api}.{method}()", "api": api})
    seen, uniq = set(), []
    for v in out:
        key = (v["file"], v["line"], v["call"])
        if key not in seen:
            seen.add(key)
            uniq.append(v)
    return uniq


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description="小程序 API 调用一致性检查")
    parser.add_argument("--format", choices=["human", "json"], default="human")
    parser.add_argument(
        "--modules-dir", default=str(API_MODULES), help="api 模块目录（测试用）"
    )
    parser.add_argument(
        "--source-root", default=str(MINIAPP_SRC), help="待扫描源码根目录（测试用）"
    )
    args = parser.parse_args(argv)

    apis = collect_api_methods(Path(args.modules_dir))
    if not apis:
        print(f"没有解析到任何 api 模块：{args.modules_dir}", file=sys.stderr)
        return 2

    calls = collect_calls(iter_source_files(Path(args.source_root)), set(apis))
    violations = find_violations(apis, calls)

    if args.format == "json":
        print(json.dumps(
            {"api_count": len(apis), "call_count": len(calls), "violations": violations},
            ensure_ascii=False, indent=2,
        ))
    else:
        print(f"api 模块: {len(apis)} 个；调用点: {len(calls)} 处")
        if not violations:
            print("未发现「调用了模块中不存在的方法」")
        else:
            print(f"\n发现 {len(violations)} 处调用不存在的方法：")
            for v in violations:
                print(f"  {v['file']}:{v['line']}  {v['call']}")

    return 1 if violations else 0


if __name__ == "__main__":
    sys.exit(main())
