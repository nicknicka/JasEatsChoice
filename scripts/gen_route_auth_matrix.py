#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成「后端路由 × 鉴权档位」清单（#576 验收标准第 2 条）。

`SecurityConfig` 改成默认拒绝后，「哪条路由匿名可调」不再能从路由本身看出来，
必须对着 `SecurityConfig` 的显式放行清单读。这个脚本把两者拼起来：

    SecurityConfig 的 permitAll 清单  ×  Controller 的 @PreAuthorize  ×  全部后端路由
    -> 每条「方法 + 路径」一个档位：公开 / 登录 / 管理员

**必须带方法**：`GET /v1/recipe/**` 放行只覆盖读，`POST /v1/recipe`、
`PUT /v1/recipe/toggle-favorite/{id}` 这些写接口仍要求登录。只按路径归档会把写接口
误报成公开——所以这里不复用 `check_routes.py` 的路径集合（它不带方法），
自己按注解扫描出 (方法, 路径) 对。

档位判定顺序（先命中先定档）：
  1. 命中 `SecurityConfig` 里显式登记的 (方法, 路径模式) -> 公开
     （不带方法的放行模式视为匹配所有方法）
  2. Controller 类级或方法级带 `hasRole('ADMIN')` / `hasAnyAuthority('admin:*')`
     或路径落在 `/admin`、`/v1/admin`、`/v1/tutorial/admin` 下 -> 管理员
  3. 其余 -> 登录（默认拒绝覆盖）

用法：
    python3 scripts/gen_route_auth_matrix.py            # 写 docs/technical/route-auth-matrix.md
    python3 scripts/gen_route_auth_matrix.py --check    # 只校验文档是否最新（CI 用）
    python3 scripts/gen_route_auth_matrix.py --stdout   # 打印到标准输出
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
SECURITY_CONFIG = (
    REPO_ROOT
    / "apps/server/src/main/java/com/xx/jaseatschoicejava/config/SecurityConfig.java"
)
CONTROLLER_DIR = REPO_ROOT / "apps/server/src/main/java/com/xx/jaseatschoicejava/controller"
OUTPUT = REPO_ROOT / "docs/technical/route-auth-matrix.md"

ADMIN_PATH_PREFIXES = ("/admin", "/v1/admin", "/v1/tutorial/admin")

MAPPING_METHODS = {
    "GetMapping": "GET",
    "PostMapping": "POST",
    "PutMapping": "PUT",
    "DeleteMapping": "DELETE",
    "PatchMapping": "PATCH",
}


def normalize(path: str) -> str:
    path = path.split("?", 1)[0].strip()
    path = re.sub(r"/+", "/", path)
    if len(path) > 1:
        path = path.rstrip("/")
    return path or "/"


def to_segments(path: str) -> "list[str | None]":
    segments = []
    for seg in normalize(path).strip("/").split("/"):
        segments.append(None if re.fullmatch(r"\{[^}]*\}", seg) else seg)
    return segments


def scan_annotations(source: str) -> "list[tuple[int, str, str]]":
    """扫出 HTTP 映射注解，返回 (位置, 注解名, 括号内参数原文)。括号配平避免截断。"""
    found = []
    for name in list(MAPPING_METHODS) + ["RequestMapping"]:
        for m in re.finditer(r"@" + name + r"\s*(\()?", source):
            if not m.group(1):
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


def annotation_paths(args: str) -> "list[str]":
    cleaned = re.sub(
        r"\b(value|path|produces|consumes|headers|params|name|method)\s*=\s*", "", args
    )
    candidates = re.findall(r'"([^"]*)"', cleaned)
    return [c for c in candidates if c.startswith("/")]


def annotation_http_methods(name: str, args: str) -> "list[str]":
    """从注解取 HTTP 方法。@GetMapping 直接给出；@RequestMapping 读 method = ...。"""
    if name in MAPPING_METHODS:
        return [MAPPING_METHODS[name]]
    found = re.findall(r"RequestMethod\.([A-Z]+)", args)
    return found or ["ALL"]


def proxy_roles(source: str) -> "list[tuple[int, str, str]]":
    """扫出所有 @PreAuthorize 注解，返回 (位置, 注解名, 括号内参数原文)。

    与 scan_annotations 分开：映射注解与方法签名的相对位置决定「这条路由属于谁」，
    而权限注解可以出现在类上或方法上，需要独立的位置判断。
    """
    found = []
    for m in re.finditer(r"@PreAuthorize\s*(\()?", source):
        if not m.group(1):
            found.append((m.start(), "PreAuthorize", ""))
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
        found.append((m.start(), "PreAuthorize", source[start + 1 : i]))
    return sorted(found)


def _admin_annotations(source: str) -> "list[int]":
    """所有「需要管理员」注解的位置。"""
    pattern = re.compile(r"hasRole\(\s*'ADMIN'\s*\)|hasAuthority\(\s*'admin:")
    return [pos for pos, _n, args in proxy_roles(source) if pattern.search(args)]


def method_signature_positions(source: str) -> "list[int]":
    """方法签名的起始位置。

    用缩进 + 访问修饰符识别：这些 Controller 的方法都缩进 4 空格。
    标注块（注解 + 签名）是以签名为右端点的区间，前后两个签名之间正好是一个方法的注解区。
    """
    return [
        m.start()
        for m in re.finditer(r"\n[ \t]+(?:public|private|protected)\s", source)
    ]


def controller_routes(source: str) -> "tuple[list[tuple[str, str]], set[tuple[str, str]]]":
    """解析单个 Controller。

    返回 (全部 (方法, 路径), 管理员 (方法, 路径))。
    管理员判定：类级 @PreAuthorize(ADMIN) -> 整类；方法级 -> 仅该方法的映射。
    """
    class_match = re.search(r"\bclass\s+\w+", source)
    if not class_match:
        return [], set()
    class_pos = class_match.start()
    annotations = scan_annotations(source)
    admin_positions = _admin_annotations(source)
    signatures = method_signature_positions(source)
    class_admin = any(pos < class_pos for pos in admin_positions)

    base_paths = [""]
    for pos, name, args in annotations:
        if pos > class_pos:
            break
        paths = annotation_paths(args)
        if paths:
            base_paths = paths
            break

    routes: list[tuple[str, str]] = []
    admin_routes: set[tuple[str, str]] = set()

    def owning_block(pos: int) -> "tuple[int, int]":
        """映射注解所属方法的标注区 (左开, 右闭]。

        右端点 = 该注解之后最近的方法签名；左端点 = 该签名之前最近的方法签名。
        @PreAuthorize 写在 @PostMapping 之前或之后都落在这个区间内，因此都能被归属；
        而「上一个方法的注解」落在左端点之外，不会被误算进来。
        """
        after = [s for s in signatures if s > pos]
        right = min(after) if after else len(source)
        before = [s for s in signatures if s < pos]
        left = max(before) if before else class_pos
        return left, right

    for pos, name, args in annotations:
        if pos < class_pos or (name not in MAPPING_METHODS and name != "RequestMapping"):
            continue
        left, right = owning_block(pos)
        method_admin = any(left < admin_pos <= right for admin_pos in admin_positions)
        method_paths = annotation_paths(args)
        http_methods = annotation_http_methods(name, args)
        for base in base_paths:
            for path in method_paths or [""]:
                full = normalize(f"{base}{path}")
                for http_method in http_methods:
                    routes.append((http_method, full))
                    if class_admin or method_admin:
                        admin_routes.add((http_method, full))
    return routes, admin_routes


def load_all_routes() -> "tuple[set[tuple[str, str]], set[tuple[str, str]]]":
    """全仓 Controller 的 (方法, 路径) 与其中需管理员的一批。"""
    all_routes: set[tuple[str, str]] = set()
    admin_routes: set[tuple[str, str]] = set()
    for java_file in sorted(CONTROLLER_DIR.rglob("*.java")):
        text = java_file.read_text(encoding="utf-8", errors="replace")
        if "Mapping(" not in text:
            continue
        routes, admins = controller_routes(text)
        all_routes.update(routes)
        admin_routes.update(admins)
    return all_routes, admin_routes


# ---------------------------------------------------------------- SecurityConfig


def public_patterns() -> "list[tuple[str | None, str]]":
    """从 SecurityConfig 抽出 permitAll 的 (HTTP 方法, 路径模式)。

    方法为 None 表示该模式对所有方法放行（`requestMatchers("/files/**")`）；
    带方法的（`requestMatchers(HttpMethod.GET, "/v1/recipe/**")`）只覆盖该方法。
    """
    source = SECURITY_CONFIG.read_text(encoding="utf-8")
    patterns: list[tuple[str | None, str]] = []
    for match in re.finditer(r"\.requestMatchers\s*\(", source):
        start = match.end() - 1
        depth, i = 0, start
        while i < len(source):
            if source[i] == "(":
                depth += 1
            elif source[i] == ")":
                depth -= 1
                if depth == 0:
                    break
            i += 1
        args = source[start + 1 : i]
        if "permitAll" not in source[i : i + 60]:
            continue
        http_method = None
        method_match = re.search(r"HttpMethod\.([A-Z]+)", args)
        if method_match:
            http_method = method_match.group(1)
        for candidate in re.findall(r'"([^"]*)"', args):
            if candidate.startswith("/") and candidate != "/**":
                patterns.append((http_method, candidate))
    return patterns


def pattern_covers(http_method: str | None, pattern: str, route_method: str, route: str) -> bool:
    """(方法, 模式) 是否覆盖某条具体路由。方法为 None 视为通配。"""
    if http_method and http_method != route_method:
        return False

    p = to_segments(normalize(pattern))
    r = to_segments(normalize(route))

    if p and p[-1] == "**":
        head = p[:-1]
        if len(r) < len(head):
            return False
        for ps, rs in zip(head, r):
            if ps in ("*", "**") or ps is None:
                continue
            if rs is None or ps != rs:
                return False
        return True

    if len(p) != len(r):
        return False
    for ps, rs in zip(p, r):
        if ps in ("*", "**") or ps is None:
            continue
        if rs is None or ps != rs:
            return False
    return True


# ---------------------------------------------------------------- 档位


def classify(method: str, route: str, public: "list[tuple[str | None, str]]",
             admin_routes: "set[tuple[str, str]]") -> str:
    if any(pattern_covers(pm, pp, method, route) for pm, pp in public):
        return "公开"
    if (method, route) in admin_routes or route.startswith(ADMIN_PATH_PREFIXES):
        return "管理员"
    return "登录"


def build_rows() -> "list[tuple[str, str, str]]":
    all_routes, admin_routes = load_all_routes()
    public = public_patterns()
    rows = [
        (method, route, classify(method, route, public, admin_routes))
        for method, route in sorted(all_routes)
    ]
    return rows


# ---------------------------------------------------------------- 渲染


def render(rows: "list[tuple[str, str, str]]") -> str:
    counts = {"公开": 0, "登录": 0, "管理员": 0}
    for _m, _r, tier in rows:
        counts[tier] = counts.get(tier, 0) + 1

    lines = [
        "# 后端路由鉴权档位清单",
        "",
        "生成方式：`python3 scripts/gen_route_auth_matrix.py`。",
        "数据来源：静态扫描 `controller/` 下的映射注解（含 HTTP 方法），",
        "叠加 `SecurityConfig` 的显式 `permitAll` 清单与 `@PreAuthorize` 判定。",
        "",
        "**按「方法 + 路径」归档**：`GET /v1/recipe/**` 放行只覆盖读，",
        "`POST /v1/recipe`、`PUT /v1/recipe/toggle-favorite/{id}` 等写接口仍要求登录。",
        "",
        f"本轮共 **{len(rows)}** 条：",
        "",
        "| 档位 | 条数 | 含义 |",
        "|---|---:|---|",
        f"| 公开 | {counts['公开']} | 匿名可调（显式登记在 `SecurityConfig`） |",
        f"| 登录 | {counts['登录']} | 默认拒绝覆盖，匿名返回 401 |",
        f"| 管理员 | {counts['管理员']} | 需 `ROLE_ADMIN` 或 `admin:*` 权限 |",
        "",
        "改路由或改 `SecurityConfig` 后重跑本脚本即可，勿手改下表。",
        "",
    ]

    for tier in ("公开", "管理员", "登录"):
        subset = [(m, r) for m, r, t in rows if t == tier]
        lines.append(f"## {tier}（{len(subset)} 条）")
        lines.append("")
        lines.append("| 方法 | 路径 |")
        lines.append("|---|---|")
        lines.extend(f"| `{m}` | `{r}` |" for m, r in subset)
        lines.append("")

    return "\n".join(lines)


def main(argv: "list[str] | None" = None) -> int:
    parser = argparse.ArgumentParser(description="生成后端路由鉴权档位清单")
    parser.add_argument("--check", action="store_true", help="只校验文档是否最新")
    parser.add_argument("--stdout", action="store_true", help="打印到标准输出")
    args = parser.parse_args(argv)

    content = render(build_rows())

    if args.stdout:
        print(content)
        return 0

    if args.check:
        current = OUTPUT.read_text(encoding="utf-8") if OUTPUT.exists() else ""
        if current != content:
            print("路由鉴权档位清单已过期：请重跑 scripts/gen_route_auth_matrix.py",
                  file=sys.stderr)
            return 1
        print(f"路由鉴权档位清单最新（{OUTPUT.relative_to(REPO_ROOT)}）")
        return 0

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(content, encoding="utf-8")
    print(f"已写入 {OUTPUT.relative_to(REPO_ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
