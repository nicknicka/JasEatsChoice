from __future__ import annotations

import argparse
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib import font_manager
from matplotlib.patches import FancyArrowPatch, FancyBboxPatch


OUTPUT_FILES = [
    "fig_3_1_architecture",
    "fig_3_2_modules",
    "fig_3_3_er",
    "fig_4_1_recommendation",
    "fig_4_2_order_flow",
    "fig_5_1_performance",
]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="生成毕业论文插图")
    parser.add_argument("--output-dir", required=True, help="输出目录")
    return parser.parse_args()


def configure_fonts() -> None:
    preferred = [
        "PingFang SC",
        "Heiti SC",
        "Hiragino Sans GB",
        "Microsoft YaHei",
        "SimHei",
        "Arial Unicode MS",
    ]
    installed = {f.name for f in font_manager.fontManager.ttflist}
    available = [name for name in preferred if name in installed]
    plt.rcParams["font.sans-serif"] = available or ["DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False


def setup_canvas(figsize=(14, 8), title: str | None = None):
    fig, ax = plt.subplots(figsize=figsize)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis("off")
    if title:
        ax.text(50, 98, title, ha="center", va="top", fontsize=18, fontweight="bold")
    return fig, ax


def draw_box(
    ax,
    x,
    y,
    w,
    h,
    text,
    fc="#F7F9FC",
    ec="#345995",
    fontsize=12,
    lw=1.8,
    radius=0.08,
    linespacing=1.25,
):
    patch = FancyBboxPatch(
        (x, y),
        w,
        h,
        boxstyle=f"round,pad=0.012,rounding_size={radius * min(w, h)}",
        linewidth=lw,
        edgecolor=ec,
        facecolor=fc,
    )
    ax.add_patch(patch)
    ax.text(
        x + w / 2,
        y + h / 2,
        text,
        ha="center",
        va="center",
        fontsize=fontsize,
        linespacing=linespacing,
    )


def draw_container(ax, x, y, w, h, label, ec="#7A8CA5", fc="#FFFFFF"):
    patch = FancyBboxPatch(
        (x, y),
        w,
        h,
        boxstyle="round,pad=0.01,rounding_size=1.2",
        linewidth=1.5,
        edgecolor=ec,
        facecolor=fc,
        linestyle="--",
    )
    ax.add_patch(patch)
    ax.text(x + 2, y + h - 2.5, label, ha="left", va="top", fontsize=12, fontweight="bold", color="#2C3E50")


def draw_arrow(
    ax,
    x1,
    y1,
    x2,
    y2,
    text="",
    color="#52616B",
    lw=1.8,
    style="-|>",
    connection="arc3",
    text_dx=0,
    text_dy=1.8,
    fontsize=10,
):
    arrow = FancyArrowPatch(
        (x1, y1),
        (x2, y2),
        arrowstyle=style,
        mutation_scale=14,
        linewidth=lw,
        color=color,
        connectionstyle=connection,
    )
    ax.add_patch(arrow)
    if text:
        ax.text(
            (x1 + x2) / 2 + text_dx,
            (y1 + y2) / 2 + text_dy,
            text,
            ha="center",
            va="center",
            fontsize=fontsize,
            color=color,
            bbox={"boxstyle": "round,pad=0.18", "fc": "white", "ec": "none", "alpha": 0.92},
        )


def save_figure(fig, output_dir: Path, stem: str) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)
    fig.tight_layout()
    fig.savefig(output_dir / f"{stem}.png", dpi=220, bbox_inches="tight")
    fig.savefig(output_dir / f"{stem}.svg", bbox_inches="tight")
    plt.close(fig)


def architecture_diagram(output_dir: Path) -> None:
    fig, ax = setup_canvas(figsize=(15, 8.8), title="佳食宜选系统总体架构图")
    draw_container(ax, 4, 67, 92, 21, "客户端层")
    draw_container(ax, 4, 33, 92, 28, "业务服务层")
    draw_container(ax, 4, 8, 92, 19, "数据与外部能力层")

    client_boxes = [
        (7, 71.5, 18, 10.5, "用户桌面端\nElectron + Vue 3"),
        (28.5, 71.5, 18, 10.5, "商家桌面端\nElectron + Vue 3"),
        (50, 71.5, 18, 10.5, "管理支撑端\nVue 3 + Element Plus"),
        (71.5, 71.5, 18, 10.5, "微信小程序端\nUniApp + Vue 3"),
    ]
    for box in client_boxes:
        draw_box(ax, *box, fc="#EAF3FF")

    draw_box(
        ax,
        10,
        49,
        80,
        8.8,
        "统一接口入口\nSpringBoot 控制器层（78 个控制器 / 497 个接口）",
        fc="#FFF5E6",
        ec="#C97B2A",
        fontsize=12.5,
    )

    service_boxes = [
        (6, 36.5, 16, 10.5, "推荐引擎\n多策略召回"),
        (24.5, 36.5, 16, 10.5, "卡路里管理\n营养记录"),
        (43, 36.5, 16, 10.5, "订单中心\n订单 / 拼单"),
        (61.5, 36.5, 14, 10.5, "即时通讯\nNetty"),
        (78, 36.5, 16, 10.5, "AI 助手\n识别与问答"),
    ]
    for box in service_boxes:
        draw_box(ax, *box, fc="#F6F8FB")

    data_boxes = [
        (7, 12, 20, 9.8, "MySQL\n72 张表定义"),
        (30, 12, 18, 9.8, "Redis\n缓存与热点数据"),
        (51, 12, 18, 9.8, "Netty WebSocket\n实时消息通道"),
        (72, 12, 21, 9.8, "外部服务\n智谱 AI / 地图 / 天气 /\n短信 / 支付"),
    ]
    for box in data_boxes:
        draw_box(ax, *box, fc="#EEF8EE", ec="#4C956C", fontsize=11.5)

    for x, _, w, _, _ in client_boxes:
        draw_arrow(ax, x + w / 2, 71.5, x + w / 2, 57.8)
    for x, _, w, _, _ in service_boxes:
        draw_arrow(ax, x + w / 2, 49, x + w / 2, 47.2)
    draw_arrow(ax, 14, 36.5, 17, 21.8, "写入 / 查询", text_dx=-1.8, text_dy=0.8)
    draw_arrow(ax, 32.5, 36.5, 17, 21.8, "健康记录", text_dx=-1.0, text_dy=0.6)
    draw_arrow(ax, 50.5, 36.5, 39, 21.8, "状态缓存", text_dx=0.2, text_dy=0.6, connection="arc3,rad=-0.02")
    draw_arrow(ax, 68.5, 36.5, 60, 21.8, "实时推送", text_dx=1.2, text_dy=0.6)
    draw_arrow(ax, 86, 36.5, 82.5, 21.8, "能力调用", text_dx=2.0, text_dy=0.6)

    save_figure(fig, output_dir, "fig_3_1_architecture")


def module_diagram(output_dir: Path) -> None:
    fig, ax = setup_canvas(figsize=(15, 8.8), title="佳食宜选功能模块图")
    draw_box(ax, 31, 76, 38, 10, "佳食宜选智能餐饮系统", fc="#FFF5E6", ec="#C97B2A", fontsize=15)
    draw_container(ax, 4, 26, 92, 42, "模块分解视图", fc="#FCFDFE")

    module_boxes = [
        (7, 35, 20, 24, "用户端\n个性化推荐\n卡路里管理\n订单管理\n社交聊天\n收藏与食谱\n个人中心", "#EAF3FF", "#345995"),
        (29, 35, 20, 24, "商家端\n订单处理\n菜单管理\n店铺信息\n营业统计\n评价中心\n消息系统", "#EAF3FF", "#345995"),
        (51, 35, 20, 24, "平台支撑\n接口文档\n统一响应\n权限框架\n教程通知\n节日推荐\n系统配置", "#EEF8EE", "#4C956C"),
        (73, 35, 20, 24, "核心能力\n推荐策略\n群拼单\n内容提取\n菜品识别\n智能对话\nSSE 优化", "#EEF8EE", "#4C956C"),
    ]
    for x, y, w, h, text, fc, ec in module_boxes:
        draw_box(ax, x, y, w, h, text, fc=fc, ec=ec, fontsize=11.4, linespacing=1.18)
        draw_arrow(ax, 50, 76, x + w / 2, y + h, connection="arc3,rad=0", text_dy=2.6)

    draw_box(ax, 20, 15, 24, 8.8, "统一后端服务\nSpringBoot + MyBatis-Plus", fc="#F6F8FB", fontsize=11.5)
    draw_box(ax, 56, 15, 24, 8.8, "共享数据与通信\nMySQL / Redis / Netty", fc="#F6F8FB", fontsize=11.5)
    draw_arrow(ax, 39, 35, 32, 23.8, "业务承载", text_dx=-1.8, text_dy=1.6, connection="arc3,rad=0.12")
    draw_arrow(ax, 61, 35, 68, 23.8, "数据协同", text_dx=1.8, text_dy=1.6, connection="arc3,rad=-0.12")

    ax.text(50, 7, "用户端、商家端、平台支撑与核心算法能力共同构成系统完整的功能闭环。", ha="center", fontsize=11, color="#425466")
    save_figure(fig, output_dir, "fig_3_2_modules")


def er_diagram(output_dir: Path) -> None:
    fig, ax = setup_canvas(figsize=(15, 8.8), title="核心数据实体关系图")
    draw_container(ax, 4, 38, 92, 45, "交易与菜品主链", fc="#FCFDFE")
    draw_container(ax, 4, 8, 44, 24, "聊天链路", fc="#FCFDFE")
    draw_container(ax, 52, 8, 44, 24, "健康与收藏链路", fc="#FCFDFE")

    entities = [
        (40, 63, 18, 12, "t_user\n用户信息"),
        (72, 63, 18, 12, "t_merchant\n商家信息"),
        (72, 43, 18, 12, "t_dish\n菜品信息"),
        (18, 43, 18, 12, "t_order\n订单主表"),
        (46, 43, 18, 12, "t_order_dish\n订单明细"),
        (8, 13, 18, 12, "t_chat_session\n会话信息"),
        (30, 13, 16, 12, "t_chat_msg\n聊天消息"),
        (54, 13, 18, 12, "t_calorie_record\n热量记录"),
        (76, 13, 16, 12, "t_collection /\n t_recipe\n收藏与食谱"),
    ]
    colors = {
        "trade": ("#EAF3FF", "#345995"),
        "social": ("#F6F8FB", "#345995"),
        "health": ("#EEF8EE", "#4C956C"),
    }
    for i, box in enumerate(entities):
        if i <= 4:
            fc, ec = colors["trade"]
        elif i <= 6:
            fc, ec = colors["social"]
        else:
            fc, ec = colors["health"]
        draw_box(ax, *box, fc=fc, ec=ec, fontsize=11.0)

    draw_arrow(ax, 49, 63, 27, 55, "1:N", connection="arc3,rad=0.02", text_dx=-1.6, text_dy=2.0)
    draw_arrow(ax, 81, 63, 81, 55, "1:N", text_dx=3.2, text_dy=0.3)
    draw_arrow(ax, 81, 63, 36, 55, "1:N", connection="arc3,rad=0.03", text_dx=-1.8, text_dy=1.8)
    draw_arrow(ax, 36, 49, 46, 49, "1:N", text_dy=3.0)
    draw_arrow(ax, 72, 49, 64, 49, "1:N", text_dy=3.0)
    draw_arrow(ax, 49, 63, 17, 25, "1:N", connection="arc3,rad=0.06", text_dx=-2.0, text_dy=1.8)
    draw_arrow(ax, 26, 19, 30, 19, "1:N", text_dy=3.0)
    draw_arrow(ax, 49, 63, 63, 25, "1:N", connection="arc3,rad=-0.04", text_dx=1.6, text_dy=1.6)
    draw_arrow(ax, 81, 43, 63, 25, "1:N", connection="arc3,rad=0.02", text_dx=2.0, text_dy=1.6)
    draw_arrow(ax, 49, 63, 84, 25, "1:N", connection="arc3,rad=-0.07", text_dx=2.2, text_dy=1.8)

    ax.text(50, 5, "该关系图围绕交易、聊天和健康管理三条主线组织实体关系，重点体现商家、用户、订单与菜品之间的核心外键关联。", ha="center", fontsize=11, color="#425466")
    save_figure(fig, output_dir, "fig_3_3_er")


def recommendation_diagram(output_dir: Path) -> None:
    fig, ax = setup_canvas(figsize=(15, 8.8), title="个性化推荐引擎处理流程图")
    draw_container(ax, 4, 61, 92, 18, "输入层", fc="#FCFDFE")
    draw_container(ax, 4, 34, 92, 18, "召回与排序层", fc="#FCFDFE")
    draw_container(ax, 18, 10, 64, 16, "解释与输出层", fc="#FCFDFE")

    draw_box(ax, 9, 65, 18, 10, "用户画像\n偏好 / 目标 / 历史", fc="#EAF3FF", fontsize=11.5)
    draw_box(ax, 31, 65, 18, 10, "上下文输入\n时间 / 天气 / 位置", fc="#EAF3FF", fontsize=11.5)
    draw_box(ax, 57, 63.5, 28, 13, "多策略召回\n画像 40% / 协同 30%\n热门 20% / 上下文 10%", fc="#EAF3FF", fontsize=11.2)

    draw_box(ax, 16, 38, 20, 10, "候选过滤\n库存 / 下架 / 拒绝记录", fc="#F6F8FB", fontsize=11.2)
    draw_box(ax, 42, 38, 20, 10, "排序与融合\n权重整合 + 规则修正", fc="#F6F8FB", fontsize=11.2)
    draw_box(ax, 68, 38, 18, 10, "业务校验\n结果稳定输出", fc="#F6F8FB", fontsize=11.2)

    draw_box(ax, 22, 13, 22, 8.8, "推荐理由生成\n偏好 / 热度 / 时段因素", fc="#EEF8EE", ec="#4C956C", fontsize=10.8)
    draw_box(ax, 50, 13, 22, 8.8, "推荐结果\n菜品 / 商家列表", fc="#EEF8EE", ec="#4C956C", fontsize=11.2)

    draw_arrow(ax, 27, 70, 57, 70, "画像特征", text_dx=0.0, text_dy=3.0)
    draw_arrow(ax, 49, 70, 57, 70, "上下文特征", text_dx=0.0, text_dy=-3.0)
    draw_arrow(ax, 71, 63.5, 26, 48, "召回候选", connection="arc3,rad=0.18", text_dx=-2.0, text_dy=2.6)
    draw_arrow(ax, 36, 43, 42, 43, "规则过滤", text_dy=3.0)
    draw_arrow(ax, 62, 43, 68, 43, "结果校验", text_dy=3.0)
    draw_arrow(ax, 52, 38, 33, 21.8, "规则解释", connection="arc3,rad=0.02", text_dx=-3.0, text_dy=2.4)
    draw_arrow(ax, 44, 17.4, 50, 17.4, "可读化输出", text_dy=2.8)
    draw_arrow(ax, 77, 38, 62, 21.8, "结构化返回", connection="arc3,rad=-0.05", text_dx=2.8, text_dy=2.2)

    ax.text(50, 6, "推荐服务将召回、规则过滤、排序融合与本地理由生成串成完整闭环，以提升结果可读性和可接受度。", ha="center", fontsize=11, color="#425466")
    save_figure(fig, output_dir, "fig_4_1_recommendation")


def order_flow_diagram(output_dir: Path) -> None:
    fig, ax = setup_canvas(figsize=(15, 8.8), title="订单处理与即时通知流程图")
    lane_specs = [
        (2, "用户端", "#FCFDFE"),
        (21, "订单服务", "#FCFDFE"),
        (40, "支付服务", "#FCFDFE"),
        (59, "消息通道", "#FCFDFE"),
        (78, "商家端", "#FCFDFE"),
    ]
    for x, label, fc in lane_specs:
        draw_container(ax, x, 10, 17, 76, label, fc=fc)

    draw_box(ax, 4, 68, 13, 8.6, "提交订单", fc="#EAF3FF", fontsize=11.2)
    draw_box(ax, 23, 68, 13, 8.6, "校验价格与库存", fc="#F6F8FB", fontsize=10.8)
    draw_box(ax, 23, 54, 13, 8.6, "生成待支付订单", fc="#F6F8FB", fontsize=10.8)
    draw_box(ax, 42, 54, 13, 8.6, "发起支付", fc="#FFF5E6", ec="#C97B2A", fontsize=11.0)
    draw_box(ax, 42, 40, 13, 8.8, "支付成功\n返回支付结果", fc="#FFF5E6", ec="#C97B2A", fontsize=10.6)
    draw_box(ax, 23, 40, 13, 8.8, "回写支付信息\n状态改为待接单", fc="#F6F8FB", fontsize=10.0)
    draw_box(ax, 61, 26, 13, 8.8, "推送下单消息", fc="#EEF8EE", ec="#4C956C", fontsize=10.8)
    draw_box(ax, 80, 26, 13, 8.8, "接收新订单", fc="#EAF3FF", fontsize=11.0)
    draw_box(ax, 80, 12, 13, 8.8, "商家接单 / 出餐\n更新处理状态", fc="#EAF3FF", fontsize=10.2)
    draw_box(ax, 61, 12, 13, 8.8, "推送状态变更", fc="#EEF8EE", ec="#4C956C", fontsize=10.4)
    draw_box(ax, 4, 12, 13, 8.8, "查看进度\n完成评价 / 复购", fc="#EAF3FF", fontsize=10.4)

    draw_arrow(ax, 17, 72.3, 23, 72.3, "下单请求", text_dy=2.8)
    draw_arrow(ax, 29.5, 68, 29.5, 62.6, "校验通过", text_dx=3.8, text_dy=0.2)
    draw_arrow(ax, 36, 58.3, 42, 58.3, "创建支付单", text_dy=2.8)
    draw_arrow(ax, 48.5, 54, 48.5, 48.8, "支付处理", text_dx=3.8, text_dy=0.2)
    draw_arrow(ax, 42, 44.4, 36, 44.4, "支付完成", text_dy=2.8)
    draw_arrow(ax, 36, 42.4, 61, 30.4, connection="arc3,rad=-0.05")
    draw_arrow(ax, 74, 30.4, 80, 30.4, "商家接收", text_dy=2.8)
    draw_arrow(ax, 86.5, 26, 86.5, 20.8, "状态处理", text_dx=3.8, text_dy=0.2)
    draw_arrow(ax, 80, 16.4, 74, 16.4, "状态回传", text_dy=2.8)
    draw_arrow(ax, 61, 16.4, 17, 16.4, "状态同步", text_dy=2.8)

    ax.text(50, 6, "订单链路先完成支付确认，再进入商家处理与消息同步环节，整体流程比上一版更直观，便于说明待支付与待接单两个状态转换。", ha="center", fontsize=11, color="#425466")
    save_figure(fig, output_dir, "fig_4_2_order_flow")


def performance_chart(output_dir: Path) -> None:
    categories = ["SSE 总链路\n(ms)", "决策阶段\n(ms)", "缓存接口\n(ms)"]
    before = [36870, 11796, 200]
    after = [12000, 1000, 30]

    fig, ax = plt.subplots(figsize=(12, 7))
    x = range(len(categories))
    width = 0.32
    bars1 = ax.bar([i - width / 2 for i in x], before, width=width, label="优化前", color="#9DB4C0")
    bars2 = ax.bar([i + width / 2 for i in x], after, width=width, label="优化后", color="#4C78A8")

    ax.set_title("关键性能指标优化效果图", fontsize=18, fontweight="bold")
    ax.set_xticks(list(x))
    ax.set_xticklabels(categories, fontsize=12)
    ax.set_ylabel("耗时（毫秒）", fontsize=12)
    ax.legend()
    ax.grid(axis="y", linestyle="--", alpha=0.4)

    for bars in [bars1, bars2]:
        for bar in bars:
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width() / 2, height, f"{int(height)}", ha="center", va="bottom", fontsize=10)

    ax.text(
        0.5,
        -0.16,
        "数据依据：SSE 流程分析报告与 Redis 缓存优化报告。缓存接口优化后平均响应约为 20ms 到 40ms，图中取中位值 30ms。",
        ha="center",
        va="top",
        transform=ax.transAxes,
        fontsize=10,
        color="#425466",
    )
    fig.tight_layout()
    save_figure(fig, output_dir, "fig_5_1_performance")


def main() -> None:
    args = parse_args()
    output_dir = Path(args.output_dir)
    configure_fonts()
    architecture_diagram(output_dir)
    module_diagram(output_dir)
    er_diagram(output_dir)
    recommendation_diagram(output_dir)
    order_flow_diagram(output_dir)
    performance_chart(output_dir)


if __name__ == "__main__":
    main()
