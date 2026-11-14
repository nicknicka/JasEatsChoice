#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""gen_route_auth_matrix.py 里那些纯函数的单元测试（标准库 unittest，零第三方依赖）。

重点固定两处曾经写错的语义（都是「明明该低权限却报成公开」的方向）：

  1. **方法粒度**：`GET /v1/recipe/**` 的放行不能覆盖 `PUT /v1/recipe/toggle-favorite/{id}`。
     早期版本只按路径归档，把写接口误报成匿名可调。
  2. **字面量模式不反吃占位符段**：`/v1/merchant/register`（字面）不能覆盖
     `/v1/merchant/{merchantId}`（商家详情，需登录）。早期复用了 check_routes 的双向宽松匹配，
     后者对占位符是对称的，于是把商家详情误判成了公开。

跑法：在仓库根目录执行
    python3 -m unittest discover -s scripts -p 'test_*.py' -v
"""
import unittest

from gen_route_auth_matrix import (
    annotation_http_methods,
    annotation_paths,
    controller_routes,
    normalize,
    pattern_covers,
    to_segments,
)


class TestNormalize(unittest.TestCase):
    def test_trailing_slash_dropped_except_root(self):
        self.assertEqual(normalize("/v1/recipe/"), "/v1/recipe")
        self.assertEqual(normalize("/"), "/")

    def test_query_string_dropped(self):
        self.assertEqual(normalize("/v1/recipe/all?page=1"), "/v1/recipe/all")


class TestPatternCoversMethod(unittest.TestCase):
    """方法粒度：放行带方法时必须只覆盖该方法。"""

    def test_get_pattern_does_not_cover_put(self):
        self.assertTrue(pattern_covers("GET", "/v1/recipe/**", "GET", "/v1/recipe/today"))
        self.assertFalse(
            pattern_covers("GET", "/v1/recipe/**", "PUT", "/v1/recipe/toggle-favorite/1"),
        )

    def test_get_pattern_does_not_cover_post_collection(self):
        # POST /v1/recipe 是新建食谱，仍需登录
        self.assertFalse(pattern_covers("GET", "/v1/recipe/**", "POST", "/v1/recipe"))

    def test_methodless_pattern_covers_every_method(self):
        self.assertTrue(pattern_covers(None, "/files/**", "GET", "/files/a.png"))
        self.assertTrue(pattern_covers(None, "/files/**", "POST", "/files/a.png"))


class TestPatternCoversPath(unittest.TestCase):
    """路径覆盖：字面量段不能反吃路由里的占位符段。"""

    def test_literal_does_not_cover_placeholder(self):
        # /v1/merchant/register 是字面模式；/v1/merchant/{merchantId} 是商家详情
        self.assertFalse(
            pattern_covers(None, "/v1/merchant/register", "GET", "/v1/merchant/{merchantId}"),
        )

    def test_literal_covers_itself(self):
        self.assertTrue(pattern_covers(None, "/v1/merchant/register", "POST", "/v1/merchant/register"))

    def test_double_star_covers_deeper_path(self):
        self.assertTrue(
            pattern_covers(None, "/uploads/**", "GET", "/uploads/avatar/x.png"),
        )

    def test_double_star_matches_prefix_itself(self):
        self.assertTrue(pattern_covers(None, "/uploads/**", "GET", "/uploads"))

    def test_double_star_respects_literal_prefix(self):
        self.assertFalse(pattern_covers(None, "/uploads/**", "GET", "/files/x.png"))

    def test_empty_star_segment_matches_anything(self):
        self.assertTrue(pattern_covers(None, "/v1/*/today", "GET", "/v1/recipe/today"))

    def test_segment_count_must_match(self):
        self.assertFalse(pattern_covers(None, "/v1/recipe", "GET", "/v1/recipe/today"))


class TestAnnotationParsing(unittest.TestCase):
    def test_get_mapping_yields_get(self):
        self.assertEqual(annotation_http_methods("GetMapping", '"/today"'), ["GET"])

    def test_request_mapping_reads_method_attribute(self):
        self.assertEqual(
            annotation_http_methods("RequestMapping", 'value="/x", method=RequestMethod.PUT'),
            ["PUT"],
        )

    def test_request_mapping_without_method_is_wildcard(self):
        self.assertEqual(annotation_http_methods("RequestMapping", '"/x"'), ["ALL"])

    def test_annotation_paths_picks_leading_slash_only(self):
        self.assertEqual(
            annotation_paths('value = "/v1/x", produces = "application/json"'),
            ["/v1/x"],
        )


class TestControllerRoutes(unittest.TestCase):
    """类级前缀 × 方法级路径，以及管理员判定的归属。"""

    def test_class_prefix_is_applied(self):
        source = '''
            @RestController
            @RequestMapping("/v1/recipe")
            public class RecipeController {
                @GetMapping("/today")
                public Object today() { return null; }
            }
        '''
        routes, _admins = controller_routes(source)
        self.assertIn(("GET", "/v1/recipe/today"), routes)

    def test_class_level_admin_marks_whole_class(self):
        source = '''
            @RestController
            @RequestMapping("/admin/dishes")
            @PreAuthorize("hasRole('ADMIN')")
            public class AdminDishController {
                @GetMapping("")
                public Object list() { return null; }
            }
        '''
        routes, admins = controller_routes(source)
        self.assertIn(("GET", "/admin/dishes"), routes)
        self.assertIn(("GET", "/admin/dishes"), admins)

    def test_methods_without_admin_annotation_are_not_admin(self):
        source = '''
            @RestController
            @RequestMapping("/v1/recipe")
            public class RecipeController {
                @GetMapping("/today")
                public Object today() { return null; }
            }
        '''
        _routes, admins = controller_routes(source)
        self.assertNotIn(("GET", "/v1/recipe/today"), admins)

    def test_admin_annotation_after_mapping_belongs_to_that_method(self):
        # 仓库里 WalletController 的写法：@PostMapping 在 @PreAuthorize 之前
        source = '''
            @RestController
            @RequestMapping("/v1/wallet")
            public class WalletController {
                @PostMapping("/recharge")
                @PreAuthorize("hasRole('ADMIN')")
                public Object recharge() { return null; }

                @PostMapping("/withdraw")
                public Object withdraw() { return null; }
            }
        '''
        _routes, admins = controller_routes(source)
        self.assertIn(("POST", "/v1/wallet/recharge"), admins)
        # 提现是「只允许本人操作」，不能被上一个方法的注解外溢成管理员接口
        self.assertNotIn(("POST", "/v1/wallet/withdraw"), admins)

    def test_admin_annotation_before_mapping_belongs_to_that_method(self):
        source = '''
            @RestController
            @RequestMapping("/admin/x")
            public class XController {
                @GetMapping("/a")
                public Object a() { return null; }

                @PreAuthorize("hasAuthority('admin:x:list')")
                @GetMapping("/b")
                public Object b() { return null; }
            }
        '''
        _routes, admins = controller_routes(source)
        self.assertIn(("GET", "/admin/x/b"), admins)
        self.assertNotIn(("GET", "/admin/x/a"), admins)


class TestSegments(unittest.TestCase):
    def test_placeholder_becomes_none(self):
        self.assertEqual(to_segments("/v1/recipe/{id}"), ["v1", "recipe", None])

    def test_literal_stays_literal(self):
        self.assertEqual(to_segments("/v1/recipe/today"), ["v1", "recipe", "today"])


if __name__ == "__main__":
    unittest.main()
