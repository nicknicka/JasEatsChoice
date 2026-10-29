#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""check_miniapp_api.py 里那些纯函数的单元测试（标准库 unittest，零第三方依赖）。

跑法：在仓库根目录执行
    python3 -m unittest discover -s scripts -p 'test_*.py' -v

重点覆盖开发过程中实测踩到的四类误报——它们都曾让一次性版本的脚本给出错误结论：
  1. ES6 简写属性导出
  2. 注释/正则里的反引号
  3. JS 内建方法
  4. 嵌套对象与函数参数的键
"""
import contextlib
import io
import tempfile
import unittest
from pathlib import Path

from check_miniapp_api import (
    JS_BUILTINS,
    collect_api_methods,
    collect_calls,
    find_violations,
    iter_source_files,
    main,
    object_keys,
    strip_noise,
)


class TestStripNoise(unittest.TestCase):
    def test_removes_line_and_block_comments(self):
        src = "const a = 1; // 行注释\n/* 块\n注释 */\nconst b = 2;"
        out = strip_noise(src)
        self.assertIn("const a = 1", out)
        self.assertIn("const b = 2", out)
        self.assertNotIn("行注释", out)
        self.assertNotIn("块", out)

    def test_backticks_inside_comment_do_not_swallow_code(self):
        """注释里的反引号（ai.js 的真实情况）不能开启模板串解析。"""
        src = "// 说明：```json 格式\nconst after = 1;"
        out = strip_noise(src)
        self.assertIn("const after = 1", out)

    def test_backticks_inside_regex_do_not_swallow_code(self):
        """正则字面量里的反引号同样不能开启模板串解析。"""
        src = "let x = s.replace(/^```json\\s*\\n?/i, '');\nconst after = 2;"
        out = strip_noise(src)
        self.assertIn("const after = 2", out)

    def test_template_literal_with_interpolation_is_removed(self):
        src = "const u = `/v1/x/${id}/y`;\nconst after = 3;"
        out = strip_noise(src)
        self.assertIn("const after = 3", out)
        self.assertNotIn("/v1/x/", out)

    def test_strings_are_removed(self):
        src = "const a = 'has { brace';\nconst b = \"also { brace\";"
        out = strip_noise(src)
        self.assertNotIn("{", out.replace(" ", ""))


class TestObjectKeys(unittest.TestCase):
    def test_shorthand_property_is_collected(self):
        """ES6 简写 `{ foo, bar }` 必须被识别为导出。"""
        src = "export const api = { foo, bar, baz: 1 }"
        start = src.index("{")
        self.assertEqual(object_keys(src, start), {"foo", "bar", "baz"})

    def test_nested_object_keys_are_ignored(self):
        """嵌套对象的键不算顶层导出。"""
        src = "export const api = { outer: { inner1: 1, inner2: 2 }, top: 3 }"
        start = src.index("{")
        self.assertEqual(object_keys(src, start), {"outer", "top"})

    def test_function_params_are_ignored(self):
        """箭头函数参数名不能被当成导出键。"""
        src = "export const api = { run: async (data, onMessage, onComplete) => data, other: 1 }"
        start = src.index("{")
        self.assertEqual(object_keys(src, start), {"run", "other"})

    def test_member_access_is_not_a_key(self):
        """`CONST.VALUE` 里的属性名不能被当成导出键。"""
        src = "export const api = { run: () => post(API.CHAT, data), stop: 1 }"
        start = src.index("{")
        keys = object_keys(src, start)
        self.assertEqual(keys, {"run", "stop"})
        self.assertNotIn("CHAT", keys)


class TestCollectApiMethods(unittest.TestCase):
    def test_collects_shorthand_export(self):
        with tempfile.TemporaryDirectory() as d:
            (Path(d) / "demo.js").write_text(
                "const helper = (x) => x\nexport const demoApi = { helper, other: () => 1 }\n"
                "export default demoApi\n",
                encoding="utf-8",
            )
            apis = collect_api_methods(Path(d))
        self.assertIn("demoApi", apis)
        self.assertIn("helper", apis["demoApi"])
        self.assertIn("other", apis["demoApi"])


class TestFindViolations(unittest.TestCase):
    def test_reports_missing_method(self):
        apis = {"demoApi": {"exists"}}
        calls = [("a.vue", 1, "demoApi", "exists"), ("a.vue", 2, "demoApi", "missing")]
        out = find_violations(apis, calls)
        self.assertEqual(len(out), 1)
        self.assertEqual(out[0]["call"], "demoApi.missing()")

    def test_js_builtins_are_not_reported(self):
        """`messageApi.map()` 这类内建方法不能被当成 API 调用。"""
        apis = {"messageApi": {"send"}}
        calls = [("a.js", 1, "messageApi", "map"), ("a.js", 2, "messageApi", "trim")]
        self.assertEqual(find_violations(apis, calls), [])

    def test_builtin_list_contains_common_methods(self):
        for m in ("map", "filter", "trim", "includes", "then"):
            self.assertIn(m, JS_BUILTINS)


class TestEndToEnd(unittest.TestCase):
    """端到端：用真实的临时目录跑一遍 main()。

    这一组是 #134 验收标准「脚本能检出人为植入的违规（验证它不是恒真通过）」
    的可追溯证据——收录进 make check 后每次都会重跑。
    """

    def _make_tree(self, d, call_source):
        mods = Path(d) / "api/modules"
        mods.mkdir(parents=True)
        (mods / "demo.js").write_text(
            "export const demoApi = {\n"
            "  known: (x) => x\n"
            "}\n"
            "export default demoApi\n",
            encoding="utf-8",
        )
        src = Path(d) / "pages"
        src.mkdir()
        (src / "a.vue").write_text(call_source, encoding="utf-8")
        return mods, src

    def test_clean_tree_passes(self):
        with tempfile.TemporaryDirectory() as d:
            mods, src = self._make_tree(d, "demoApi.known(1)\n")
            with contextlib.redirect_stdout(io.StringIO()):
                rc = main(["--modules-dir", str(mods), "--source-root", str(src)])
        self.assertEqual(rc, 0, "干净代码库应通过")

    def test_planted_violation_is_detected(self):
        with tempfile.TemporaryDirectory() as d:
            mods, src = self._make_tree(d, "demoApi.known(1)\ndemoApi.plantedMissing(2)\n")
            with contextlib.redirect_stdout(io.StringIO()):
                rc = main(["--modules-dir", str(mods), "--source-root", str(src)])
        self.assertEqual(rc, 1, "植入的违规必须被检出（否则检查是恒真通过）")

    def test_violation_reports_file_and_line(self):
        with tempfile.TemporaryDirectory() as d:
            mods, src = self._make_tree(d, "// 占位\ndemoApi.plantedMissing(2)\n")
            apis = collect_api_methods(mods)
            calls = collect_calls(iter_source_files(src), set(apis))
            out = find_violations(apis, calls)
        self.assertEqual(len(out), 1)
        self.assertEqual(out[0]["line"], 2, "行号应指向真实调用处")
        self.assertTrue(out[0]["file"].endswith("a.vue"))


if __name__ == "__main__":
    unittest.main()
