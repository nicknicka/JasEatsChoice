#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""check_routes.py 里那些纯函数的单元测试（标准库 unittest，零第三方依赖）。

跑法：在仓库根目录执行
    python3 -m unittest discover -s scripts -p 'test_*.py' -v
"""
import unittest

from check_routes import (
    _annotation_paths,
    _controller_routes,
    diff_routes,
    match_path,
    normalize,
    paths_match,
)


class TestNormalize(unittest.TestCase):
    def test_query_string_is_dropped(self):
        self.assertEqual(normalize("/v1/dishes?page=1"), "/v1/dishes")

    def test_trailing_slash_is_dropped_except_root(self):
        self.assertEqual(normalize("/v1/dishes/"), "/v1/dishes")
        self.assertEqual(normalize("/"), "/")

    def test_duplicate_slashes_collapse(self):
        self.assertEqual(normalize("/v1//dishes"), "/v1/dishes")


class TestTemplateMatching(unittest.TestCase):
    def test_placeholder_matches_one_segment(self):
        self.assertTrue(match_path("/v1/dishes/{dishId}", {"/v1/dishes/{dishId}"}))

    def test_placeholder_swallows_a_real_segment(self):
        self.assertTrue(match_path("/v1/dishes/{dishId}", {"/v1/dishes/42"}))

    def test_placeholder_does_not_match_a_missing_segment(self):
        # 前端少了尾段 = 真实错位：少一个 /date 就 404
        self.assertFalse(match_path("/v1/dishes", {"/v1/dishes/{dishId}/audit"}))

    def test_matching_is_symmetric_on_placeholders(self):
        # 占位符出现在哪一侧都要能对齐：一侧抓下来的路由保留模板，另一侧可能是具体段
        self.assertTrue(paths_match("/v1/dishes/{dishId}", "/v1/dishes/42"))
        self.assertTrue(paths_match("/v1/dishes/42", "/v1/dishes/{dishId}"))

    def test_metacharacters_stay_literal(self):
        self.assertTrue(paths_match("/v1/a.b/{id}", "/v1/a.b/9"))
        self.assertFalse(paths_match("/v1/a.b/{id}", "/v1/aXb/9"))


class TestDiffRoutes(unittest.TestCase):
    backend = {
        "/v1/dishes/{dishId}",
        "/calorie-records/user/{userId}/date/{date}",
        "/admin/system/logs",
    }

    def test_exact_and_templated_paths_pass(self):
        report = diff_routes(
            {"/v1/dishes/{dishId}", "/calorie-records/user/{userId}/date/{date}"},
            self.backend,
            set(),
        )
        self.assertEqual(report["missing"], [])
        self.assertEqual(report["new"], [])

    def test_backend_rename_is_reported(self):
        report = diff_routes({"/admin/logs/system"}, self.backend, set())
        self.assertEqual(report["missing"], ["/admin/logs/system"])
        self.assertEqual(report["new"], ["/admin/logs/system"])

    def test_partial_path_is_reported_as_drift(self):
        # 前端只写到 /date，后端需要 /date/{date}
        report = diff_routes({"/calorie-records/user/{userId}/date"}, self.backend, set())
        self.assertIn("/calorie-records/user/{userId}/date", report["missing"])

    def test_baseline_swallows_known_drift(self):
        report = diff_routes({"/admin/logs/system"}, self.backend, {"/admin/logs/system"})
        self.assertEqual(report["missing"], ["/admin/logs/system"])
        self.assertEqual(report["new"], [])

    def test_new_drift_escapes_the_baseline(self):
        baseline = {"/admin/logs/system"}
        report = diff_routes(
            {"/admin/logs/system", "/admin/logs/login"}, self.backend, baseline
        )
        self.assertEqual(report["new"], ["/admin/logs/login"])

    def test_fixed_drift_is_reported_so_baseline_can_tighten(self):
        report = diff_routes(set(), self.backend, {"/admin/logs/system"})
        self.assertEqual(report["missing"], [])
        self.assertEqual(report["fixed"], ["/admin/logs/system"])

    def test_empty_inputs_are_harmless(self):
        report = diff_routes(set(), set(), set())
        self.assertEqual(report, {"missing": [], "new": [], "fixed": []})


class TestAnnotationParsing(unittest.TestCase):
    def test_bare_path(self):
        self.assertEqual(_annotation_paths('"/v1/dishes"'), ["/v1/dishes"])

    def test_keyed_value_is_unwrapped(self):
        self.assertEqual(_annotation_paths('value = "/v1/dishes", produces = "application/json"'), ["/v1/dishes"])

    def test_multiple_paths_in_array(self):
        self.assertEqual(_annotation_paths('value = {"/a", "/b"}'), ["/a", "/b"])

    def test_pathless_mapping_yields_nothing(self):
        self.assertEqual(_annotation_paths(""), [])

    def test_controller_combines_base_and_methods(self):
        source = '''
        @RestController
        @RequestMapping("/v1/dishes")
        public class DishController {
            @GetMapping
            public R list() { return null; }

            @PostMapping("/batch")
            public R batch() { return null; }
        }
        '''
        self.assertEqual(sorted(_controller_routes(source)), ["/v1/dishes", "/v1/dishes/batch"])

    def test_nested_parentheses_survive_scanning(self):
        # @Operation(summary = "x (草稿)") 这种嵌套括号曾经会把正则截断
        source = '''
        @RestController
        @RequestMapping("/v1/dishes")
        public class DishController {
            @GetMapping("/{id}")
            @Operation(summary = "查详情 (含草稿)")
            public R one() { return null; }
        }
        '''
        self.assertEqual(_controller_routes(source), ["/v1/dishes/{id}"])


if __name__ == "__main__":
    unittest.main()
