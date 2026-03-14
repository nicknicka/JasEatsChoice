# TODO - FatSecret API 对接计划

> **创建日期**：2026-03-14
> **优先级**：P1（中等优先级）
> **状态**：待规划

---

## 📋 对接背景

当前已集成中国食物成分表（本地数据库），为提升国际化能力和数据覆盖率，计划对接FatSecret API。

**FatSecret API优势**：
- ✅ 全球最大的食物与营养数据库
- ✅ 支持56个国家，24种语言
- ✅ 覆盖通用食物、品牌食物、餐厅食物
- ✅ 完全免费使用
- ✅ 数据经过验证准确

---

## 🎯 对接目标

### 主要目标
1. 作为中国食物成分表的补充数据源
2. 支持国际化食物查询（英文、其他语言）
3. 覆盖品牌食物和餐厅连锁店数据
4. 提升食物匹配成功率

### 使用场景
- 用户查询"麦当劳巨无霸"等品牌食物
- 用户查询西式菜品（如"Pizza"、"Spaghetti"）
- 中国食物成分表中未收录的食物

---

## 📝 实施步骤

### Phase 1: 注册与准备（预计1小时）

- [ ] **1.1 注册FatSecret开发者账号**
  - 访问：https://platform.fatsecret.com/platform-api
  - 申请API Key和Secret
  - 记录申请信息到配置文件

- [ ] **1.2 了解API文档**
  - 阅读官方API文档
  - 了解认证方式（OAuth 1.0）
  - 了解请求限制和配额

- [ ] **1.3 测试API可用性**
  - 使用Postman/curl测试基础接口
  - 验证API Key有效性
  - 测试食物搜索功能

### Phase 2: Java SDK集成（预计2-3小时）

- [ ] **2.1 添加Maven依赖**
  ```xml
  <!-- FatSecret可能使用HTTP客户端，需要添加依赖 -->
  <dependency>
      <groupId>org.apache.httpcomponents</groupId>
      <artifactId>httpclient</artifactId>
      <version>4.5.14</version>
  </dependency>
  ```

- [ ] **2.2 创建配置类**
  - 创建 `FatSecretConfig.java`
  - 配置API Key和Secret
  - 配置OAuth认证参数

- [ ] **2.3 创建Service接口**
  - 创建 `FatSecretService.java`
  - 定义方法：
    - `searchFood(String keyword)` - 搜索食物
    - `getFoodDetails(Long foodId)` - 获取食物详情
    - `getBrandFoods(String brand)` - 获取品牌食物

- [ ] **2.4 实现Service**
  - 创建 `FatSecretServiceImpl.java`
  - 实现OAuth 1.0认证逻辑
  - 实现API调用方法
  - 实现数据解析（JSON转对象）

### Phase 3: 数据模型映射（预计1-2小时）

- [ ] **3.1 创建DTO对象**
  - `FatSecretFoodDTO.java` - 食物基本信息
  - `FatSecretNutrientDTO.java` - 营养信息
  - `FatSecretServingDTO.java` - 份量信息

- [ ] **3.2 数据转换**
  - 创建 `FatSecretDataConverter.java`
  - 将FatSecret数据格式转换为 `NutritionInfo`
  - 处理单位转换（如：1g = 1cal）

### Phase 4: 集成到现有系统（预计2-3小时）

- [ ] **4.1 修改NutritionAnalysisService**
  ```java
  // 查询优先级：
  // 1. 中国食物成分表（本地数据库）
  // 2. FatSecret API（远程）
  // 3. 估算算法（兜底）
  ```

- [ ] **4.2 实现缓存策略**
  - 使用Redis缓存FatSecret查询结果
  - 设置合理的过期时间（如7天）
  - 减少API调用次数

- [ ] **4.3 添加日志和监控**
  - 记录API调用次数
  - 记录命中率统计
  - 监控响应时间

### Phase 5: 测试与优化（预计2小时）

- [ ] **5.1 单元测试**
  - 测试API调用逻辑
  - 测试数据转换
  - 测试缓存机制

- [ ] **5.2 集成测试**
  - 测试完整的营养分析流程
  - 测试中英文混合查询
  - 测试异常情况处理

- [ ] **5.3 性能优化**
  - 优化API调用频率
  - 优化缓存策略
  - 优化超时设置

---

## 🔧 技术实现要点

### OAuth 1.0 认证

FatSecret使用OAuth 1.0认证，需要实现签名逻辑：

```java
// 示例：OAuth签名步骤
1. 收集请求参数
2. 按字母顺序排序参数
3. 生成签名基础字符串
4. 使用HMAC-SHA1算法生成签名
5. 将签名添加到请求头
```

### API端点

| 接口 | 说明 | 示例 |
|------|------|------|
| `foods.search` | 搜索食物 | `?search_expression=apple` |
| `food.get` | 获取食物详情 | `?food_id=12345` |
| `brands.get` | 获取品牌列表 | `?brand_id=123` |

### 响应数据格式

```json
{
  "food": {
    "food_id": "12345",
    "food_name": "Apple",
    "food_type": "Generic",
    "servings": {
      "serving": [
        {
          "serving_id": "123",
          "serving_description": "1 medium (3-1/4\" dia)",
          "metric_serving_amount": "182.0",
          "metric_serving_unit": "g",
          "calories": "95",
          "protein": "0.47",
          "fat": "0.31",
          "carbohydrate": "25.13"
        }
      ]
    }
  }
}
```

---

## ⚠️ 注意事项

### 限制与约束
- ✅ **免费使用**：无费用，但需遵守使用条款
- ⚠️ **请求限制**：可能有每日调用次数限制（需查看文档）
- ⚠️ **数据归属**：使用数据时需标注数据来源
- ⚠️ **网络依赖**：需要稳定的网络连接

### 风险评估
| 风险 | 影响 | 应对措施 |
|------|------|---------|
| API服务不稳定 | 中 | 使用缓存+本地数据库兜底 |
| 请求次数超限 | 低 | 实施请求限流和缓存策略 |
| 数据格式变更 | 低 | 使用DTO封装，隔离变化 |
| 认证失效 | 低 | 定期检查API Key有效性 |

---

## 📊 成功标准

### 功能指标
- [ ] 成功集成FatSecret API
- [ ] API调用成功率 > 95%
- [ ] 平均响应时间 < 2秒
- [ ] 缓存命中率 > 70%

### 业务指标
- [ ] 食物匹配成功率提升 20%+
- [ ] 支持英文食物名称查询
- [ ] 覆盖主流品牌食物（麦当劳、肯德基等）

---

## 🔄 备选方案

如果FatSecret API不适合，可考虑：

| 备选方案 | 优势 | 劣势 |
|---------|------|------|
| **USDA FoodData Central** | 完全免费、官方权威 | 英文为主、需翻译 |
| **Edamam Nutrition API** | 易用性好、支持自然语言 | 免费额度有限（400次/月） |
| **Nutritionix API** | 数据量大、品牌食物全 | 免费版功能受限 |
| **继续扩展本地数据库** | 无网络依赖、速度快 | 需要手动维护更新 |

---

## 📚 参考资源

- **官方网站**：https://platform.fatsecret.com/platform-api
- **API文档**：https://platform.fatsecret.com/api/Default.aspx?wbrpl=wbrperf
- **开发者社区**：https://devcommunity.fatsecret.com/
- **中文支持**：https://www.fatsecret.com/cn

---

## 📅 时间规划

| 阶段 | 预计工作量 | 建议开始时间 |
|------|-----------|-------------|
| Phase 1: 注册与准备 | 1小时 | 待定 |
| Phase 2: Java SDK集成 | 2-3小时 | 待定 |
| Phase 3: 数据模型映射 | 1-2小时 | 待定 |
| Phase 4: 集成到现有系统 | 2-3小时 | 待定 |
| Phase 5: 测试与优化 | 2小时 | 待定 |
| **总计** | **8-11小时** | **1-2个工作日** |

---

## 🎯 下一步行动

**当前状态**：中国食物成分表已集成并运行稳定

**触发条件**（满足任一即可开始）：
- [ ] 用户反馈英文食物查询需求增加
- [ ] 中国食物成分表匹配率 < 80%
- [ ] 业务扩展到国际化市场
- [ ] 有充足的开发时间（1-2天）

**负责人**：待分配
**预计完成时间**：待定

---

**最后更新**：2026-03-14
**文档版本**：v1.0
