# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

---
# 数据库账号 
root
# 数据库密码
123456
# 数据库名称
jia_shi_yi_xuan
# 语言设置
请从现在开始始终用简体中文回答，避免使用英文术语。

## 项目文档体系

- **项目概述**：佳食宜选.md
- **详细需求**：产品需求说明书（PRD）.md
- **PRD补充**：PRD_补充完善.md（记录系统现有实现与设计的差异）
- **技术实现**：佳食宜选技术实现指导.md
- **后端API**：后端API文档.md


## 已实现核心功能
### 用户端
- 个性化饮食推荐
- 卡路里精准管理
- 全流程订单管理
- AI饮食助手
- 社交聊天与互动
- 我的收藏与食谱
- 个人中心与设置

### 商家端
- 订单管理系统
- 菜单与菜品管理
- 店铺信息管理
- 营业统计分析
- 评价中心
- 消息与聊天系统

## 排除功能
- 线下门店收银系统
- 供应链仓储管理
- 第三方配送调度（仅集成配送状态查询）

## 开发规范
- 回答问题时请使用中文
- 代码实现需遵循技术文档中的技术栈要求
- 保持代码结构清晰，符合Vue 3最佳实践
- 使用Element Plus组件库实现统一UI风格

## 技术栈
- 后端：SpringBoot + MyBatis-Plus + MySQL + Redis + Netty
- 前端：Electron + Vue 3 + Element Plus
- 小程序：UniApp
