# LocationController 统一改造计划（摘要版）

## 目标

将定位相关请求统一收敛到 `/v1/location/*`，`AMapController` 不做保留了直接下线。

## 改造范围

### 后端

- `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/LocationController.java`
- `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/AMapController.java`
- `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/service/impl/LocationServiceImpl.java`

### Electron 前端

- `JasEatsChoiceFront/src/renderer/src/api/location.js`
- `JasEatsChoiceFront/src/renderer/src/composables/useAmapLocation.js`
- `JasEatsChoiceFront/src/renderer/src/views/user/HomeContent.vue`
- `JasEatsChoiceFront/src/renderer/src/components/CommonMapLocationPicker.vue`
- `JasEatsChoiceFront/src/renderer/src/api/index.js`
- `JasEatsChoiceFront/src/renderer/src/config/index.js`

### UniApp

- `JasEatsChoiceUniApp/api/urlEnum.js`
- `JasEatsChoiceUniApp/src/api/urlEnum.js`

## 目标接口规范

- `GET /v1/location`
- `GET /v1/location/search`
- `GET /v1/location/geocode`
- `GET /v1/location/reverse-geocode`
- `GET /v1/location/cascader`

## 分阶段计划

## Phase 1：后端入口收口（1 天）

1. `LocationController` 作为唯一主入口。
2. 在代码中直接删除 `AMapController`，不保留兼容代理层。
3. 下线前执行一次全仓检索，确认无 `/v1/amap` 调用后再合并发布。

交付物：
- 后端仅保留 `/v1/location/*` 一套入口。
- 代码仓中不存在 `AMapController`。

## Phase 2：Electron 调用与命名统一（1 天）

1. 在 `location.js` 增加标准方法名 `reverseGeocode`。
2. 移除 `regeocode` 兼容别名，调用方全部切换到 `reverseGeocode`。
3. 调用点改为使用 `reverseGeocode`。
4. 搜索参数统一为 `keywords`，后端继续兼容 `address`。

交付物：
- 主仓不再新增 `regeocode` 调用。
- 搜索/逆编码/定位链路可用。

## Phase 3：UniApp 常量对齐（0.5 天）

1. 修正 `urlEnum` 中 location 接口注释与真实请求方式不一致问题（当前实现是 GET）。
2. 两份 `urlEnum.js` 保持同步。

交付物：
- 注释与实现一致。
- 双文件无差异。

## Phase 4：联调与灰度（0.5~1 天）

1. 回归接口：`location`、`search`、`geocode`、`reverse-geocode`、`cascader`。
2. 回归页面：地图选点、地址搜索、首页定位、附近商家定位参数。
3. 发布前后执行 API 烟测，重点验证无旧路径依赖。

交付物：
- 回归报告。
- 上线后验证记录（确认 `/v1/amap` 已彻底移除）。

## 风险与应对

1. 风险：旧调用方仍依赖 `/v1/amap`。
- 应对：发布前全仓检索 + 联调环境冒烟 + 生产回滚预案（必要时快速回滚本次提交）。

2. 风险：前端响应结构判断不统一（`code`/`success` 混用）。
- 应对：在 Phase 2 统一判定逻辑。

3. 风险：UniApp 双文件维护漂移。
- 应对：提交前做双文件 diff 校验。

## 验收标准

1. 新功能与历史功能均通过 `/v1/location/*` 完成。
2. 前后端仓库均无 `/v1/amap` 调用。
3. 前端代码不再出现 `regeocode` 方法调用与定义。
4. 后端不存在 `AMapController`，并通过核心定位链路回归。
