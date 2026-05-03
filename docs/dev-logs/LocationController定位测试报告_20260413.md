# LocationController 定位功能测试报告

## 1. 报告信息
- 报告日期：2026-04-13
- 目标模块：LocationController
- 测试类型：控制器单元测试（MockMvc standalone）
- 测试文件：JasEatsChoiceJava/src/test/java/com/xx/jaseatschoicejava/controller/LocationControllerTest.java

## 2. 结论摘要
- 执行结果：通过 7 项，失败 0 项
- 结论：定位相关核心接口在当前测试覆盖下返回结构正确、状态码正确、服务参数透传正确，且已具备经纬度、IP参数、请求头IP兜底三段逻辑。

## 3. 三段逻辑说明

### 3.1 第一段：经纬度优先定位
- 触发条件：同时传入 `latitude` 与 `longitude`
- 处理逻辑：后端优先按经纬度逆地理编码，不使用 IP 分支
- 输出特点：地址精度最高，返回经纬度与详细地址

### 3.2 第二段：IP定位（参数优先，请求头兜底）
- 触发条件：未传经纬度
- 处理顺序：
  - 先用请求参数 `ip`
  - 若未传或不可用，再尝试请求头 `X-Forwarded-For`、`X-Real-IP` 等
  - 最后尝试 `remoteAddr`
- 过滤规则：会过滤 `unknown`、回环地址、内网地址、链路本地地址
- 输出特点：城市级粗定位，可能只有省市区，坐标可能为空

### 3.3 第三段：无有效信息时明确失败
- 触发条件：既无有效经纬度，也无可用公网 IP
- 返回结果：
  - `success=false`
  - `code=LOCATION_PARAM_MISSING`
  - `message=未传递ip或经纬度信息无法定位`
- 目的：避免“静默空结果”，让前端明确进入手动选址或默认城市兜底

## 4. 返回结构统一规范
根据统一响应封装，接口返回结构如下：
- success：布尔值，表示请求是否成功
- code：字符串，成功时为 200
- message：字符串，成功时为 成功
- data：业务数据对象或数组

对应实现位置：
- Response 字段定义：JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/common/ResponseResult.java
- 成功返回构造：JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/common/ResponseResult.java

## 5. 接口返回结果样例（本次测试验证）

### 4.1 获取当前定位（无经纬度）
- 接口：GET /api/v1/location
- 覆盖用例：getCurrentLocation_shouldReturnSuccess
- 验证点：
  - HTTP 200
  - success = true
  - code = 200
  - data.city = 北京市
  - data.longitude = 116.397428

样例响应：
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": {
    "province": "北京市",
    "city": "北京市",
    "district": "东城区",
    "longitude": "116.397428",
    "latitude": "39.90923"
  }
}

### 4.2 获取当前定位（带经纬度）
- 接口：GET /api/v1/location?latitude=39.90923&longitude=116.397428
- 覆盖用例：getCurrentLocation_withLatLng_shouldPassParamsToService
- 验证点：
  - HTTP 200
  - success = true
  - data.address = 北京市东城区
  - 服务收到参数：latitude=39.90923，longitude=116.397428

样例响应：
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": {
    "address": "北京市东城区",
    "longitude": "116.397428",
    "latitude": "39.90923"
  }
}

### 4.3 坐标转地址
- 接口：GET /api/v1/location/reverse-geocode?lng=116.397428&lat=39.90923
- 覆盖用例：reverseGeocode_shouldReturnSuccess
- 验证点：
  - HTTP 200
  - success = true
  - code = 200
  - data.formattedAddress = 北京市东城区东华门街道

样例响应：
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": {
    "formattedAddress": "北京市东城区东华门街道",
    "province": "北京市"
  }
}

### 4.4 级联地址数据
- 接口：GET /api/v1/location/cascader
- 覆盖用例：getCascaderLocationData_shouldReturnSuccess
- 验证点：
  - HTTP 200
  - success = true
  - data[0].label = 北京市

样例响应：
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": [
    {
      "label": "北京市",
      "value": "110000"
    }
  ]
}

## 6. 覆盖与边界说明
- 本次已覆盖：
  - location 经纬度分支
  - location IP 参数分支
  - location 请求头 IP 分支
  - location 无输入失败分支
  - reverse-geocode
  - cascader
- 本次未覆盖：
  - GET /api/v1/location/geocode
  - GET /api/v1/location/search
  - 服务层真实外部调用（高德 API）链路
- 建议补充：
  - LocationServiceImpl 的外部 API 适配单元测试（模拟 status=1 但字段为空数组的场景）
  - Controller 集成测试（带 Spring 上下文与配置）

## 7. 证据索引
- 控制器路由定义：JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/LocationController.java
- 测试用例位置：JasEatsChoiceJava/src/test/java/com/xx/jaseatschoicejava/controller/LocationControllerTest.java
- 统一响应结构：JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/common/ResponseResult.java
