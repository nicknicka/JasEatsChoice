#!/usr/bin/env python3
"""扫描管理员相关表中的历史乱码，并输出可读的排查报告。"""

from __future__ import annotations

import argparse
import os
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path


MOJIBAKE_REGEX = "å|ç|æ|é|è|ä|ï|ð"


@dataclass(frozen=True)
class Target:
    table: str
    column: str
    id_column: str
    label: str


TARGETS = [
    Target("t_user", "nickname", "user_id", "用户昵称"),
    Target("t_user", "location", "user_id", "用户所在地"),
    Target("t_user", "bio", "user_id", "用户简介"),
    Target("t_dish", "name", "id", "菜品名称"),
    Target("t_dish", "description", "id", "菜品描述"),
    Target("t_dish", "audit_comment", "id", "菜品审核意见"),
    Target("hot_topic", "content", "id", "热点内容"),
    Target("hot_topic", "remark", "id", "热点备注"),
    Target("hot_topic", "review_comment", "id", "热点审核意见"),
    Target("t_merchant", "name", "id", "商家名称"),
    Target("t_merchant", "address", "id", "商家地址"),
    Target("t_merchant", "contact_name", "id", "商家联系人"),
    Target("t_merchant", "audit_reason", "id", "商家审核备注"),
    Target("announcement", "title", "id", "公告标题"),
    Target("announcement", "content", "id", "公告内容"),
    Target("t_role", "role_name", "role_id", "角色名称"),
    Target("t_role", "description", "role_id", "角色描述"),
    Target("t_permission", "permission_name", "permission_id", "权限名称"),
    Target("t_permission", "description", "permission_id", "权限描述"),
    Target("t_admin", "real_name", "admin_id", "管理员姓名"),
    Target("t_admin", "remark", "admin_id", "管理员备注"),
]

SCHEMA_TABLES = [
    "t_admin",
    "t_role",
    "t_permission",
    "t_user",
    "t_dish",
    "hot_topic",
    "t_merchant",
    "announcement",
]


def mysql(cmd_sql: str, database: str, user: str, password: str, host: str, port: str) -> str:
    cmd = [
        "mysql",
        f"-h{host}",
        f"-P{port}",
        f"-u{user}",
        f"-p{password}",
        "-D",
        database,
        "--batch",
        "--raw",
        "--skip-column-names",
        "-e",
        cmd_sql,
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        sys.stderr.write(result.stderr)
        raise SystemExit(result.returncode)
    return result.stdout.strip()


def query_target(target: Target, database: str, user: str, password: str, host: str, port: str) -> tuple[int, list[tuple[str, str, str]]]:
    preview_expr = (
        f"CONVERT(BINARY(CONVERT({target.column} USING latin1)) USING utf8mb4)"
    )
    sql = f"""
SELECT {target.id_column}, {target.column}, {preview_expr}
FROM {target.table}
WHERE {target.column} IS NOT NULL
  AND {target.column} REGEXP '{MOJIBAKE_REGEX}'
LIMIT 3;
"""
    preview_rows = []
    raw = mysql(sql, database, user, password, host, port)
    if raw:
        for line in raw.splitlines():
            parts = line.split("\t")
            while len(parts) < 3:
                parts.append("")
            preview_rows.append((parts[0], parts[1], parts[2]))

    count_sql = f"""
SELECT COUNT(*)
FROM {target.table}
WHERE {target.column} IS NOT NULL
  AND {target.column} REGEXP '{MOJIBAKE_REGEX}';
"""
    count = int(mysql(count_sql, database, user, password, host, port) or "0")
    return count, preview_rows


def query_schema_comment_summary(database: str, user: str, password: str, host: str, port: str) -> tuple[int, int]:
    tables = ",".join(f"'{table}'" for table in SCHEMA_TABLES)
    table_sql = f"""
SELECT COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = '{database}'
  AND TABLE_NAME IN ({tables})
  AND TABLE_COMMENT REGEXP '{MOJIBAKE_REGEX}';
"""
    column_sql = f"""
SELECT COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = '{database}'
  AND TABLE_NAME IN ({tables})
  AND COLUMN_COMMENT REGEXP '{MOJIBAKE_REGEX}';
"""
    table_count = int(mysql(table_sql, database, user, password, host, port) or "0")
    column_count = int(mysql(column_sql, database, user, password, host, port) or "0")
    return table_count, column_count


def build_report(database: str, user: str, password: str, host: str, port: str) -> str:
    lines = [
        "# 管理端乱码排查报告",
        "",
        f"- 数据库：`{database}`（{host}:{port}）",
        f"- 典型乱码判定：`{MOJIBAKE_REGEX}`",
        "",
        "| 表 | 字段 | 说明 | 问题数量 | 示例ID | 原始值 | 修复预览 |",
        "| --- | --- | --- | ---: | --- | --- | --- |",
    ]

    total_issues = 0
    for target in TARGETS:
        count, previews = query_target(target, database, user, password, host, port)
        total_issues += count
        if previews:
            first = previews[0]
            lines.append(
                f"| `{target.table}` | `{target.column}` | {target.label} | {count} | `{first[0]}` | `{first[1]}` | `{first[2]}` |"
            )
        else:
            lines.append(
                f"| `{target.table}` | `{target.column}` | {target.label} | {count} | - | - | - |"
            )

    table_comment_count, column_comment_count = query_schema_comment_summary(
        database, user, password, host, port
    )

    lines.extend(
        [
            "",
            "## 汇总",
            "",
            f"- 业务字段命中总数：`{total_issues}`",
            f"- 管理端相关表注释乱码数：`{table_comment_count}`",
            f"- 管理端相关列注释乱码数：`{column_comment_count}`",
            "",
            "## 说明",
            "",
            "- 该脚本只做排查，不写库。",
            "- 修复预览使用 `latin1 -> utf8mb4` 转换，适用于典型 UTF-8 被错误写入西文编码的历史数据。",
        ]
    )
    return "\n".join(lines) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(description="扫描管理员相关乱码数据")
    parser.add_argument("--host", default=os.getenv("DB_HOST", "127.0.0.1"),
                        help="MySQL 地址，默认连 dev overlay 发布到宿主机的 127.0.0.1")
    parser.add_argument("--port", default=os.getenv("DB_PORT", "3306"))
    parser.add_argument("--database", default=os.getenv("MYSQL_DATABASE", "jia_shi_yi_xuan"))
    parser.add_argument("--user", default=os.getenv("DB_USERNAME") or None,
                        help="应用账号，来自 deploy/.env 的 DB_USERNAME；不是 root")
    parser.add_argument("--password", default=os.getenv("DB_PASSWORD") or None,
                        help="来自 deploy/.env 的 DB_PASSWORD；不提供则退出，不回退默认口令")
    parser.add_argument("--output", help="输出 Markdown 文件路径")
    args = parser.parse_args()

    if not args.user or not args.password:
        parser.error(
            "缺少数据库账号或密码：先在 deploy/.env 配好 DB_USERNAME/DB_PASSWORD"
            "（bash scripts/gen-env.sh 可生成），或显式传 --user/--password。"
        )

    report = build_report(args.database, args.user, args.password, args.host, args.port)
    if args.output:
        output_path = Path(args.output)
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(report, encoding="utf-8")
    else:
        sys.stdout.write(report)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
