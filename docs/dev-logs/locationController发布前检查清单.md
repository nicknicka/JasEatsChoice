# LocationController 发布前检查清单

## 1. 代码层检查

- [ ] 后端不存在 `AMapController` 文件。
- [ ] 全仓检索无 `/v1/amap` 调用。
- [ ] Electron 端无 `regeocode` 定义与调用。
- [ ] UniApp 两份 `urlEnum.js` 的 location 注释一致且为 GET 语义。

## 2. 接口检查

- [ ] `GET /v1/location` 返回结构正常。
- [ ] `GET /v1/location/search` 可用（`keywords` 入参）。
- [ ] `GET /v1/location/geocode` 可用。
- [ ] `GET /v1/location/reverse-geocode` 可用。
- [ ] `GET /v1/location/cascader` 可用。

## 3. 页面回归

- [ ] Electron：首页定位展示正常。
- [ ] Electron：地图选点后地址回填正常。
- [ ] Electron：地址搜索结果与选中流程正常。
- [ ] UniApp：首页定位信息展示正常。
- [ ] UniApp：使用定位参数的附近商家/推荐流程正常。

## 4. 检索命令

```bash
rg -n "/v1/amap|AMapController|regeocode\(" JasEatsChoiceJava/src/main/java JasEatsChoiceFront/src/renderer/src JasEatsChoiceUniApp/api JasEatsChoiceUniApp/src/api
```

## 5. 发布与回滚

- [ ] 发布前执行一次接口烟测并记录结果。
- [ ] 发布后 30 分钟内复测核心定位链路。
- [ ] 若发现旧路径依赖，立即回滚到上一稳定版本。
