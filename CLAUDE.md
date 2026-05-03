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

文档统一归档在 `docs/` 目录下，按类型分类：

### 产品文档 (docs/product/)
- [佳食宜选.md](docs/product/佳食宜选.md) - 项目概述
- [PRD.md](docs/product/PRD.md) - 产品需求说明书

### API文档 (docs/api/)
- [后端API文档.md](docs/api/后端API文档.md) - 后端接口文档
- [前后端字段对接分析报告.md](docs/api/前后端字段对接分析报告.md) - 字段对接分析
- [接口修复计划.md](docs/api/接口修复计划.md) - 接口修复计划

### 技术文档 (docs/technical/)
- [LangChain4j_Agent系统文档.md](docs/technical/LangChain4j_Agent系统文档.md) - AI Agent系统设计
- [LangChain4j用户隔离实现文档.md](docs/technical/LangChain4j用户隔离实现文档.md) - 用户隔离实现
- [Redis缓存优化实施总结报告.md](docs/technical/Redis缓存优化实施总结报告.md) - Redis优化总结
- [Redis优化第三阶段完成报告.md](docs/technical/Redis优化第三阶段完成报告.md) - Redis优化第三阶段
- [chatMemory对比分析.md](docs/technical/chatMemory对比分析.md) - ChatMemory技术选型
- [内容抓取技术实现.md](docs/technical/内容抓取技术实现.md) - 内容抓取技术方案
- [高德地图重构计划.md](docs/technical/高德地图重构计划.md) - 地图功能重构
- [地址功能梳理.md](docs/technical/地址功能梳理.md) - 地址功能说明
- [架构重构清理记录.md](docs/technical/架构重构清理记录.md) - 架构重构记录

### 测试文档 (docs/testing/)
- [API测试方案.md](docs/testing/API测试方案.md) - API测试方案
- [test-checklist.md](docs/testing/test-checklist.md) - 测试检查清单
- [test-report.md](docs/testing/test-report.md) - 测试报告

### 开发日志 (docs/dev-logs/)
包含SSE流程分析、LocationController改造、SupervisorPlanner JSON截断修复等开发过程记录。

### 论文文档 (docs/thesis/)
毕业论文相关文档，包括初稿、格式修订版等。

### 归档文档 (docs/archive/)
历史开发文档、AI分析报告、UniApp开发记录等已归档内容。


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
