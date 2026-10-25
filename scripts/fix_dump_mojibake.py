#!/usr/bin/env python3
"""Repair double-encoded UTF-8 (stored as cp1252-looking text) inside mysqldump files.

The live database keeps its DDL column comments double-encoded from historical ALTER
statements run under the wrong client charset; row data is repaired in-database by
build_db_seed.py, but comments can only be fixed in the dumped text."""
import re
import sys

MOJI = re.compile(r"[À-ÿŒœŠšŸŽžƒˆ˜–—‘’“”•…‰′″‹›€¢£¥¦§¨©³¹º¼½¾]{2,}")
COMMENT = re.compile(r"COMMENT '((?:[^']|'')*)'")


def looks_double_encoded(text):
    if not MOJI.search(text):
        return False
    try:
        fixed = text.encode("cp1252").decode("utf-8")
    except (UnicodeEncodeError, UnicodeDecodeError):
        return False
    return fixed != text and not MOJI.search(fixed)


def fix(match):
    body = match.group(1)
    if not looks_double_encoded(body):
        return match.group(0)
    fixed = body.encode("cp1252").decode("utf-8").replace("'", "''")
    return f"COMMENT '{fixed}'"


def main(path):
    text = open(path, encoding="utf-8").read()
    changed = [0]

    def sub(match):
        out = fix(match)
        if out != match.group(0):
            changed[0] += 1
        return out

    open(path, "w", encoding="utf-8").write(COMMENT.sub(sub, text))
    print(f"{path}: repaired {changed[0]} column comments")
    residual = sum(1 for m in COMMENT.finditer(open(path, encoding='utf-8').read())
                   if looks_double_encoded(m.group(1)))
    print(f"{path}: residual double-encoded comments: {residual}")


if __name__ == "__main__":
    for p in sys.argv[1:]:
        main(p)
