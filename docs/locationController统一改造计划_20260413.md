# LocationController 统一接入改造计划（2026-04-13）

## 1. 目标

统一使用 `/v1/location` 作为定位相关能力入口，`AMapController` 直接下线，不再保留兼容层。

## 2. 现状梳理

### 2.1 后端现状

- 已存在主入口：`LocationController`（`/v1/location`）
- 旧入口 `AMapController` 已下线
- `LocationServiceImpl` 底层已经复用 `AMapService`
- 实际风险点：旧入口和新入口的路径/命名不一致，容易造成后续调用方混用

### 2.2 前端（Electron Vue）现状

- 未发现对 `/v1/amap` 的直接调用
- 已统一走 `/v1/location/*`
- 已统一命名：前端调用使用 `reverseGeocode`
- 搜索参数存在双形态：`keywords` 与 `address`

### 2.3 前端（UniApp）现状

- 常量已使用 `/v1/location/*`
- 但注释中仍标注 `POST`，与后端当前 `GET` 实现不一致
- 存在两份 URL 枚举文件（`api/urlEnum.js` 与 `src/api/urlEnum.js`），需要保持一致

## 3. 接口统一规范（目标态）

### 3.1 标准路径

- 获取定位：`GET /v1/location`
- 地址搜索：`GET /v1/location/search`
- 地理编码：`GET /v1/location/geocode`
- 逆地理编码：`GET /v1/location/reverse-geocode`
- 行政区级联：`GET /v1/location/cascader`

### 3.2 请求参数规范

- 搜索接口统一使用 `keywords`（保留 `address` 作为兼容入参，后端内部归一）
- 逆地理编码统一使用 `lng` + `lat`
- 获取定位接口支持 `latitude` + `longitude`（有值则逆编码，无值返回空结果）

## 4. 改造范围（代码清单）

### 后端

- `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/LocationController.java`
- `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/AMapController.java`
- `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/service/impl/LocationServiceImpl.java`
- `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/service/AMapService.java`

### Electron 前端

- `JasEatsChoiceFront/src/renderer/src/api/location.js`
- `JasEatsChoiceFront/src/renderer/src/api/index.js`
- `JasEatsChoiceFront/src/renderer/src/config/index.js`
- `JasEatsChoiceFront/src/renderer/src/constants/apiConstants.js`
- `JasEatsChoiceFront/src/renderer/src/composables/useAmapLocation.js`
- `JasEatsChoiceFront/src/renderer/src/views/user/HomeContent.vue`
- `JasEatsChoiceFront/src/renderer/src/components/CommonMapLocationPicker.vue`

### UniApp

- `JasEatsChoiceUniApp/api/urlEnum.js`
- `JasEatsChoiceUniApp/src/api/urlEnum.js`

## 5. 分阶段实施计划

## 阶段 A：接口收口（后端）

1. 在 `LocationController` 明确标注“唯一业务入口”。
2. 直接删除 `AMapController`，后端仅保留 `/v1/location/*` 入口。
3. 发布前执行全仓检索，确保无 `/v1/amap` 调用残留。

验收标准：

- 任意定位能力请求都通过 `/v1/location/*` 完成。
- 后端代码中不存在 `AMapController`。

## 阶段 B：前端命名与调用归一（Electron）

1. `location.js` 使用标准命名 `reverseGeocode`，不保留 `regeocode` 别名。
2. 统一调用方改用 `reverseGeocode`（`useAmapLocation.js`、`HomeContent.vue` 等）。
3. 搜索入参统一使用 `keywords`，保留后端 `address` 兼容能力。
4. 检查 `api/index.js`、`config/index.js`、`constants/apiConstants.js` 三处配置，避免重复和命名漂移。

验收标准：

- 前端代码不再出现 `regeocode` 定义与调用。
- 定位搜索、逆编码、IP兜底链路功能保持可用。

## 阶段 C：UniApp 常量与注释对齐

1. 修正 `GEOCODE` / `REVERSE_GEOCODE` 注释中的请求方法为 `GET`。
2. 两份 `urlEnum.js` 同步改造，防止后续编译/拷贝覆盖。
3. 若未使用 `UPDATE_LOCATION`，标记为待清理常量。

验收标准：

- UniApp 文档注释与后端真实行为一致。
- 双份常量文件无差异。

## 阶段 D：测试与发布

1. 后端接口回归：
   - `/v1/location/search`
   - `/v1/location/geocode`
   - `/v1/location/reverse-geocode`
   - `/v1/location`
   - `/v1/location/cascader`
2. Electron 关键页面回归：地图选点、地址搜索、首页定位显示。
3. UniApp 关键页面回归：首页定位、地址选择、附近商家。
4. 发布前后执行 API 烟测，验证无旧路径依赖。

## 6. 风险与回滚

- 风险 1：调用方仍依赖旧路径参数结构
  - 应对：全仓检索 + 联调冒烟 + 生产回滚预案
- 风险 2：前端响应结构判断不统一（`code/success` 混用）
  - 应对：在阶段 B 做一次响应判定规范化
- 风险 3：UniApp 双份枚举文件维护不一致
  - 应对：提交前增加 diff 校验

回滚策略：

- 若发布后发现旧路径依赖，快速回滚本次提交并恢复上一稳定版本。

## 7. 建议执行顺序

1. 先做后端直接下线旧入口（阶段 A）
2. 再做 Electron 命名统一（阶段 B）
3. 同步修正 UniApp 枚举注释与常量（阶段 C）
4. 最后联调回归与灰度观察（阶段 D）
