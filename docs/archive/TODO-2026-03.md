# TODO 待办事项

> 最后更新：2026-03-14

---

## 🚀 高优先级任务

- [ ] 集成中国食物成分表到营养分析服务
  - 创建 t_nutrition 数据库表
  - 下载并解析中国食物成分表JSON数据
  - 编写数据导入脚本
  - 修改 NutritionAnalysisServiceImpl 优先查询本地营养数据
  - 测试营养分析准确性

---

## 📋 中优先级任务

### [ ] 对接 FatSecret API（国际化营养数据）

**背景**：
- 当前使用中国食物成分表（覆盖中文食物）
- FatSecret API 提供全球最大的营养数据库
- 支持品牌食物、餐厅食物（56个国家数据）
- 适合未来国际化扩展

**实施计划**：

#### 1. 注册 FatSecret API Key
- 访问：https://platform.fatsecret.com/platform-api
- 申请开发者账号
- 获取 API Key 和 Secret
- 配置 OAuth 1.0 认证

#### 2. 创建 FatSecret 配置类
```java
@Configuration
@ConfigurationProperties(prefix = "fatsecret")
@Data
public class FatSecretConfig {
    private String apiKey;
    private String apiSecret;
    private String oauthConsumerKey;
    private String oauthConsumerSecret;
}
```

#### 3. 实现 FatSecret 客户端
```java
@Service
public class FatSecretClient {
    // OAuth 1.0 签名
    // API 调用封装
    // 错误处理和重试
}
```

#### 4. 实现营养查询服务
```java
public NutritionInfo searchFood(String foodName) {
    // 1. 先查本地数据库（中国食物成分表）
    // 2. 未找到时调用 FatSecret API
    // 3. 缓存结果到本地
}
```

#### 5. 数据缓存策略
- 使用 Redis 缓存查询结果
- 缓存有效期：7天
- 减少 API 调用次数

#### 6. 测试和优化
- 测试常见食物查询
- 性能优化（API响应时间）
- 错误处理和降级策略

**参考资料**：
- API文档：https://platform.fatsecret.com/api/
- OAuth 1.0 规范
- FatSecret 开发者指南

**预估工作量**：2-3天

---

## 🔮 低优先级任务（未来优化）

- [ ] 增加USDA FoodData Central API作为备选数据源
- [ ] 实现营养数据自动更新机制
- [ ] 支持用户自定义食物营养数据
- [ ] 营养数据可视化（图表展示）
- [ ] 根据用户历史记录推荐健康菜品

---

## 📝 已完成任务 ✅

- [x] 集成智谱AI SDK并实现Function Calling功能
- [x] 实现6个AI工具函数（搜索菜品、营养分析、创建订单等）
- [x] 修复订单创建功能（真实数据库操作）
- [x] 消除硬编码问题
- [x] 创建完整的测试文档

---

## 🔄 技术债务记录

### 营养分析模块
- **当前问题**：使用估算算法，非100%准确数据
- **影响范围**：`NutritionAnalysisServiceImpl`
- **解决方案**：集成中国食物成分表（高优先级）
- **长期计划**：对接 FatSecret API（中优先级）

### 数据库字段
- **问题**：t_dish 表只有 calorie 字段，缺少蛋白质、脂肪、碳水等详细字段
- **影响**：无法存储完整的营养数据
- **解决方案**：
  1. 短期：创建独立的 t_nutrition 表
  2. 长期：在 t_dish 表增加 protein/fat/carbohydrate 字段

---

## 📅 版本规划

### v1.1（短期）
- 集成中国食物成分表
- 提升营养分析准确性

### v1.2（中期）
- 对接 FatSecret API
- 支持品牌食物和餐厅食物
- 实现营养数据缓存

### v1.3（长期）
- 多数据源融合（中国+USDA+FatSecret）
- 用户自定义食物库
- 营养数据可视化
