"""
商家端AI功能测试脚本

测试内容：
1. AI主页面渲染
2. 经营助手聊天面板
3. 经营洞察面板
4. 评价回复生成器
5. 菜品描述生成器

运行方式：
1. 先启动前端开发服务器：cd JasEatsChoiceFront && npm run dev
2. 在另一个终端运行测试：python test_merchant_ai.py
"""

from playwright.sync_api import sync_playwright
import time
import os

# 测试配置
BASE_URL = "http://localhost:5173"  # Electron开发服务器默认端口
SCREENSHOT_DIR = "/tmp/merchant_ai_test"

def ensure_screenshot_dir():
    """确保截图目录存在"""
    os.makedirs(SCREENSHOT_DIR, exist_ok=True)

def test_merchant_ai_page():
    """测试商家端AI页面"""
    print("=" * 60)
    print("商家端AI功能测试")
    print("=" * 60)

    ensure_screenshot_dir()

    with sync_playwright() as p:
        # 启动浏览器
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            viewport={"width": 1400, "height": 900},
            locale="zh-CN"
        )
        page = context.new_page()

        # 收集控制台日志
        console_logs = []
        page.on("console", lambda msg: console_logs.append(f"[{msg.type}] {msg.text}"))

        # 收集错误
        errors = []
        page.on("pageerror", lambda err: errors.append(str(err)))

        try:
            # ========== 测试1：访问商家端首页 ==========
            print("\n📋 测试1：访问商家端首页")
            page.goto(f"{BASE_URL}/merchant/home")
            page.wait_for_load_state("networkidle", timeout=30000)
            time.sleep(2)

            # 截图
            page.screenshot(path=f"{SCREENSHOT_DIR}/01_merchant_home.png", full_page=True)
            print(f"   ✅ 商家端首页加载成功")
            print(f"   📸 截图保存: {SCREENSHOT_DIR}/01_merchant_home.png")

            # ========== 测试2：检查AI菜单入口 ==========
            print("\n📋 测试2：检查AI经营助手菜单入口")

            # 查找AI经营助手菜单项
            ai_menu = page.locator("text=AI经营助手")
            if ai_menu.count() > 0:
                print(f"   ✅ AI经营助手菜单入口存在")
            else:
                print(f"   ⚠️ AI经营助手菜单入口未找到")

            # ========== 测试3：访问AI页面 ==========
            print("\n📋 测试3：访问AI经营助手页面")
            page.goto(f"{BASE_URL}/merchant/home/ai")
            page.wait_for_load_state("networkidle", timeout=30000)
            time.sleep(2)

            page.screenshot(path=f"{SCREENSHOT_DIR}/02_ai_page.png", full_page=True)
            print(f"   ✅ AI页面加载成功")
            print(f"   📸 截图保存: {SCREENSHOT_DIR}/02_ai_page.png")

            # 检查页面标题
            title = page.locator("h2:has-text('AI经营助手')")
            if title.count() > 0:
                print(f"   ✅ 页面标题正确")
            else:
                print(f"   ⚠️ 页面标题未找到")

            # ========== 测试4：检查Tab页签 ==========
            print("\n📋 测试4：检查Tab页签")
            tabs = ["经营助手", "经营洞察", "评价回复", "菜品描述"]

            for tab_name in tabs:
                tab = page.locator(f".el-tabs__item:has-text('{tab_name}')")
                if tab.count() > 0:
                    print(f"   ✅ Tab '{tab_name}' 存在")
                else:
                    print(f"   ❌ Tab '{tab_name}' 未找到")

            # ========== 测试5：测试经营助手聊天面板 ==========
            print("\n📋 测试5：测试经营助手聊天面板")

            # 确保在经营助手Tab
            chat_tab = page.locator(".el-tabs__item:has-text('经营助手')")
            if chat_tab.count() > 0:
                chat_tab.click()
                time.sleep(1)

            # 检查快捷提问
            quick_questions = page.locator(".quick-btn")
            quick_count = quick_questions.count()
            print(f"   ✅ 快捷提问按钮数量: {quick_count}")

            # 检查输入框
            input_area = page.locator(".input-area textarea, .input-area input")
            if input_area.count() > 0:
                print(f"   ✅ 输入框存在")
            else:
                print(f"   ⚠️ 输入框未找到")

            # 截图
            page.screenshot(path=f"{SCREENSHOT_DIR}/03_chat_panel.png", full_page=True)
            print(f"   📸 截图保存: {SCREENSHOT_DIR}/03_chat_panel.png")

            # ========== 测试6：测试经营洞察面板 ==========
            print("\n📋 测试6：测试经营洞察面板")

            insight_tab = page.locator(".el-tabs__item:has-text('经营洞察')")
            if insight_tab.count() > 0:
                insight_tab.click()
                time.sleep(1)

                # 检查指标卡片
                metric_cards = page.locator(".metric-card")
                metric_count = metric_cards.count()
                print(f"   ✅ 指标卡片数量: {metric_count}")

                # 检查洞察卡片
                insight_cards = page.locator(".insight-card")
                insight_count = insight_cards.count()
                print(f"   ✅ 洞察卡片数量: {insight_count}")

                page.screenshot(path=f"{SCREENSHOT_DIR}/04_insight_panel.png", full_page=True)
                print(f"   📸 截图保存: {SCREENSHOT_DIR}/04_insight_panel.png")
            else:
                print(f"   ⚠️ 经营洞察Tab未找到")

            # ========== 测试7：测试评价回复生成器 ==========
            print("\n📋 测试7：测试评价回复生成器")

            reply_tab = page.locator(".el-tabs__item:has-text('评价回复')")
            if reply_tab.count() > 0:
                reply_tab.click()
                time.sleep(1)

                # 检查评价列表
                review_cards = page.locator(".review-card")
                review_count = review_cards.count()
                print(f"   ✅ 评价卡片数量: {review_count}")

                # 点击第一个评价
                if review_count > 0:
                    review_cards.first.click()
                    time.sleep(2)

                    # 检查AI建议
                    suggestions = page.locator(".suggestion-item")
                    suggestion_count = suggestions.count()
                    print(f"   ✅ AI回复建议数量: {suggestion_count}")

                page.screenshot(path=f"{SCREENSHOT_DIR}/05_reply_panel.png", full_page=True)
                print(f"   📸 截图保存: {SCREENSHOT_DIR}/05_reply_panel.png")
            else:
                print(f"   ⚠️ 评价回复Tab未找到")

            # ========== 测试8：测试菜品描述生成器 ==========
            print("\n📋 测试8：测试菜品描述生成器")

            dish_tab = page.locator(".el-tabs__item:has-text('菜品描述')")
            if dish_tab.count() > 0:
                dish_tab.click()
                time.sleep(1)

                # 检查表单元素
                name_input = page.locator("input[placeholder*='菜品名称']")
                if name_input.count() > 0:
                    print(f"   ✅ 菜品名称输入框存在")

                    # 填写测试数据
                    name_input.fill("红烧肉")
                    print(f"   ✅ 填写菜品名称: 红烧肉")

                # 检查风格选项
                style_options = page.locator(".style-option")
                style_count = style_options.count()
                print(f"   ✅ 描述风格选项数量: {style_count}")

                # 检查生成按钮
                generate_btn = page.locator("button:has-text('生成描述')")
                if generate_btn.count() > 0:
                    print(f"   ✅ 生成描述按钮存在")

                page.screenshot(path=f"{SCREENSHOT_DIR}/06_dish_panel.png", full_page=True)
                print(f"   📸 截图保存: {SCREENSHOT_DIR}/06_dish_panel.png")
            else:
                print(f"   ⚠️ 菜品描述Tab未找到")

            # ========== 测试结果汇总 ==========
            print("\n" + "=" * 60)
            print("测试结果汇总")
            print("=" * 60)

            if errors:
                print(f"\n❌ 发现 {len(errors)} 个错误:")
                for err in errors[:5]:  # 只显示前5个错误
                    print(f"   - {err[:100]}...")
            else:
                print("\n✅ 没有发现JavaScript错误")

            print(f"\n📸 截图保存目录: {SCREENSHOT_DIR}")
            print("\n测试完成!")

        except Exception as e:
            print(f"\n❌ 测试失败: {e}")
            page.screenshot(path=f"{SCREENSHOT_DIR}/error.png", full_page=True)
            raise

        finally:
            browser.close()

if __name__ == "__main__":
    test_merchant_ai_page()
