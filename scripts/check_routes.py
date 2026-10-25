#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""前后端路由一致性检查。

前端把 API 路径集中写在一个配置模块里，后端把它们暴露成 HTTP 路由。
两边各自演进时容易错位（漏了 /v1 前缀、后端改了名字、前端调用了一个根本
不存在的接口），这类错位没有任何编译期或运行前检查能发现，只有真正点到那
个页面才会 500。

这个脚本做的事：把两侧的路径都抽出来，归一化后比对，报出前端写了但后端没有的
路径。存量差异用基线冻结，只有**新增**漂移才判失败。

零第三方依赖。用法见 main()。
"""
from __future__ import annotations

import argparse
import difflib
import json
import re
import sys
import urllib.request
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent

FRONTEND_CONFIG = REPO_ROOT / "apps/client/src/renderer/src/config/index.js"
JAVA_SRC = REPO_ROOT / "apps/server/src/main/java"
BASELINE = REPO_ROOT / "scripts/baselines/route-drift.txt"

DEFAULT_OPENAPI_URL = "http://127.0.0.1:7777/api/v3/api-docs"
OPENAPI_TIMEOUT = 4  # 秒；探活失败就退回静态解析，不让检查卡住


# ---------------------------------------------------------------- 归一化与匹配


def strip_query(path: str) -> str:
    """丢掉查询串。前端有些端点靠 ?id= 传参，路径部分才是要比的东西。"""
    return path.split("?", 1)[0]


def normalize(path: str) -> str:
    """统一形状：去查询串、去重复斜杠、去尾斜杠（根路径除外）。"""
    path = strip_query(path).strip()
    path = re.sub(r"/+", "/", path)
    if len(path) > 1:
        path = path.rstrip("/")
    return path


def to_segments(path: str) -> "list[str | None]":
    """拆成路径段；`{占位符}` 段用 None 表示，其余保持字面值。

    不翻正则：路径结构就是逐段的，占位符两侧都可能出现，按段比对天然对称，
    也顺手避开了字符串里的正则元字符要转义的问题。
    """
    segments = []
    for seg in normalize(path).strip("/").split("/"):
        segments.append(None if re.fullmatch(r"\{[^}]*\}", seg) else seg)
    return segments


def paths_match(left: str, right: str) -> bool:
    """两条路径是否落到同一个位置：段数相同，每一段相等或有一侧是占位符。"""
    a, b = to_segments(left), to_segments(right)
    if len(a) != len(b):
        return False
    for x, y in zip(a, b):
        if x is None or y is None:
            continue
        if x != y:
            return False
    return True


def match_path(frontend_path: str, backend_paths: "set[str]") -> bool:
    """前端某条路径在后端有没有落点。"""
    target = normalize(frontend_path)
    if not target:
        return False
    return any(paths_match(target, candidate) for candidate in backend_paths)


# ---------------------------------------------------------------- 纯差异计算


def diff_routes(
    frontend_paths: "set[str]", backend_paths: "set[str]", baseline: "set[str]"
) -> dict:
    """唯一的 seam：输入两侧路径集合，输出差异报告。

    - missing：前端写了、后端没有的路径
    - new：missing 里不在基线中的部分（只有它会让检查失败）
    - fixed：基线里已经消失的差异（可以顺手收紧基线）
    返回值不碰文件系统、不发网络请求，所以可以直接用 fixture 测。
    """
    missing = sorted(
        {p for p in frontend_paths if p and not match_path(p, backend_paths)}
    )
    new = [p for p in missing if p not in baseline]
    fixed = sorted(set(baseline) - set(missing))
    return {"missing": missing, "new": new, "fixed": fixed}


# ---------------------------------------------------------------- 前端路径抽取


def extract_frontend_paths(config_path: Path) -> "set[str]":
    """从前端集中定义 API 路径的模块里抽出以 / 开头的字符串字面量。

    只做词法抽取，不解析调用点：全仓库的 API 路径确实都写在那一处，
    上 AST 只会增加维护成本而覆盖不到更多真实bug。
    """
    source = config_path.read_text(encoding="utf-8")
    start = source.index("export const API_CONFIG")
    open_brace = source.index("{", start)

    depth, end = 0, open_brace
    for i in range(open_brace, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                end = i
                break

    body = source[open_brace:end]
    literals = set()
    for quoted in re.findall(r"'([^']+)'", body) + re.findall(r'"([^"]+)"', body):
        if quoted.startswith("/"):
            literals.add(normalize(quoted))
    return literals


# ---------------------------------------------------------------- 后端路由来源


def load_openapi_routes(url: str = DEFAULT_OPENAPI_URL) -> "set[str]":
    """读运行中服务暴露的 OpenAPI 文档。最贴近真实行为，但需要栈在跑。"""
    with urllib.request.urlopen(url, timeout=OPENAPI_TIMEOUT) as resp:
        docs = json.load(resp)
    return {normalize(p) for p in docs.get("paths", {})}


MAPPING_ANNOTATIONS = (
    "GetMapping",
    "PostMapping",
    "PutMapping",
    "DeleteMapping",
    "PatchMapping",
    "RequestMapping",
)


def _scan_annotations(source: str) -> "list[tuple[int, str, str]]":
    """扫出所有 HTTP 映射注解，返回 (位置, 注解名, 括号里的参数原文)。

    用括号配平而不是正则：注解参数里嵌套 producers / @Operation 之类的括号很常见，
    正则会提前截断。
    """
    found = []
    for name in MAPPING_ANNOTATIONS:
        for m in re.finditer(r"@" + name + r"\s*(\()?", source):
            if not m.group(1):
                # 无参写法：@GetMapping / @PostMapping 直接挂在方法上
                found.append((m.start(), name, ""))
                continue
            start = m.end() - 1
            depth, i = 0, start
            while i < len(source):
                if source[i] == "(":
                    depth += 1
                elif source[i] == ")":
                    depth -= 1
                    if depth == 0:
                        break
                i += 1
            found.append((m.start(), name, source[start + 1 : i]))
    return sorted(found)


def _annotation_paths(args: str) -> "list[str]":
    """从一个注解参数里取出所有路径常量。

    支持 `@GetMapping("/x")`、`@RequestMapping(value = {"/a", "/b"})`、
    `value = "/x", produces = "..."` 这些写法。
    """
    args = re.sub(
        r"\b(value|path|produces|consumes|headers|params|name|method)\s*=\s*", "", args
    )
    candidates = re.findall(r'"([^"]*)"', args)
    paths = [c for c in candidates if c.startswith("/")]
    return paths or ([candidates[0]] if len(candidates) == 1 else [])


def _controller_routes(source: str) -> "list[str]":
    """解析单个 Controller：类级 @RequestMapping 前缀 × 各方法的路径。"""
    class_pos = re.search(r"\bclass\s+\w+", source)
    if not class_pos:
        return []
    annotations = _scan_annotations(source)

    base_paths = [""]
    for pos, _name, args in annotations:
        if pos > class_pos.start():
            break
        paths = _annotation_paths(args)
        if paths:
            base_paths = paths
            break

    routes = []
    for pos, _name, args in annotations:
        if pos < class_pos.start():
            continue
        for base in base_paths:
            method_paths = _annotation_paths(args)
            # 方法注解不带路径时，Spring 把它映射到类级路径本身
            for path in method_paths or [""]:
                routes.append(f"{base}{path}")
    return routes


def load_static_routes(java_src: Path = JAVA_SRC) -> "set[str]":
    """离线解析 Controller 源码。

    仓库里那个生成 API 文档的 Python 脚本也有 Controller 解析，但它是为写 Markdown
    文档设计的，漏掉近一半路由（实测漏到本该 15 条的差异虚报成 119 条），所以这里
    自带一套只取路径的扫描：宁可不猜,也不要因为解析不全把 CI 刷红。
    """
    routes: "set[str]" = set()
    for java_file in sorted(java_src.rglob("*.java")):
        text = java_file.read_text(encoding="utf-8", errors="replace")
        if "Mapping(" not in text:
            continue
        for route in _controller_routes(text):
            routes.add(normalize(route))
    return routes


def resolve_backend_routes(source: str, url: str = DEFAULT_OPENAPI_URL) -> tuple:
    """按策略取后端路由，返回 (路由集合, 实际来源)。"""
    if source == "static":
        return load_static_routes(), "static"
    if source == "openapi":
        return load_openapi_routes(url), "openapi"
    try:
        return load_openapi_routes(url), "openapi"
    except Exception:
        return load_static_routes(), "static"


# ---------------------------------------------------------------- 基线文件


def read_baseline(path: Path = BASELINE) -> "set[str]":
    if not path.exists():
        return set()
    lines = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#"):
            lines.append(normalize(line))
    return set(lines)


def write_baseline(paths: "list[str]", path: Path = BASELINE) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    body = [
        "# 已知的前端/后端路径差异（路由漂移检查基线）。",
        "# 这个文件的语义是：这些差异已经知道，暂时不判失败；**新增**的差异才判失败。",
        "# 修好一条后端缺失后重新跑 `python3 scripts/check_routes.py --update-baseline` 收紧它。",
        "",
    ]
    body.extend(f"{p}" for p in sorted(paths))
    path.write_text("\n".join(body) + "\n", encoding="utf-8")


# ---------------------------------------------------------------- 报告


def suggest(frontend_path: str, backend_paths: "set[str]") -> "list[str]":
    """给一条漂移路径找后端最像的几条，省得人自己去翻 controller。"""
    target = normalize(frontend_path)
    return difflib.get_close_matches(target, sorted(backend_paths), n=3, cutoff=0.45)


def render_human(report: dict, backend_paths: "set[str]", source: str) -> str:
    missing, new, fixed = report["missing"], report["new"], report["fixed"]
    lines = [
        f"后端路由来源：{source}（{len(backend_paths)} 条）",
        f"已知差异：{len(missing)} 条；其中新增（判失败）：{len(new)} 条",
    ]
    if new:
        lines.append("")
        lines.append("新增漂移 —— 这些前端路径后端没有对应实现：")
        for path in new:
            lines.append(f"  - {path}")
            for candidate in suggest(path, backend_paths):
                lines.append(f"      后端相近：{candidate}")
    if fixed:
        lines.append("")
        lines.append(f"已消失的差异 {len(fixed)} 条，可用 --update-baseline 收紧基线：")
        for path in fixed[:10]:
            lines.append(f"  - {path}")
    return "\n".join(lines)


# ---------------------------------------------------------------- 入口


def main(argv: "list[str] | None" = None) -> int:
    parser = argparse.ArgumentParser(description="前后端路由一致性检查")
    parser.add_argument(
        "--source",
        choices=["auto", "openapi", "static"],
        default="auto",
        help="后端路由来源：栈在跑就读 OpenAPI，否则静态解析 Controller（默认 auto）",
    )
    parser.add_argument("--openapi-url", default=DEFAULT_OPENAPI_URL)
    parser.add_argument("--update-baseline", action="store_true", help="把当前差异写成新基线")
    parser.add_argument("--format", choices=["human", "json"], default="human")
    args = parser.parse_args(argv)

    frontend = extract_frontend_paths(FRONTEND_CONFIG)
    backend, source = resolve_backend_routes(args.source, args.openapi_url)
    baseline = read_baseline()

    report = diff_routes(frontend, backend, baseline)

    if args.update_baseline:
        write_baseline(report["missing"])
        print(f"基线已更新：{len(report['missing'])} 条 -> {BASELINE.relative_to(REPO_ROOT)}")
        return 0

    if args.format == "json":
        print(json.dumps({**report, "source": source}, ensure_ascii=False, indent=2))
    else:
        print(render_human(report, backend, source))

    if report["new"]:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
