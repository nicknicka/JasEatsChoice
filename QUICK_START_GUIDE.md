# 🚀 加菜功能 - 快速启动指南

## ✅ 已完成的工作

### 1. 数据库迁移 ✅
- ✅ 创建了 `t_add_dish_request` 表（加菜请求表）
- ✅ 创建了 `t_add_dish_setting` 表（加菜设置表）
- ✅ 修改了 `t_group_order` 表（新增支付模式字段）
- ✅ 修改了 `t_order` 表（新增加菜订单关联字段）
- ✅ 修改了 `t_order_dish` 表（新增加菜标识字段）

### 2. 后端代码开发 ✅
- ✅ 实体类（Entity）
- ✅ 枚举类（Enum）
- ✅ DTO类（6个）
- ✅ Mapper接口
- ✅ Service层（核心业务逻辑）
- ✅ Controller层（API端点）
- ✅ 定时任务（超时处理）

### 3. 前端组件开发 ✅
- ✅ 常量配置（addDishConstants.js）
- ✅ API接口（addDish.js）
- ✅ 加菜对话框（AddDishDialog.vue）
- ✅ 审核面板（AddDishReviewPanel.vue）
- ✅ GroupOrderDrawer 集成

### 4. 文档编写 ✅
- ✅ 实施总结文档
- ✅ Chat.vue 集成指南
- ✅ 部署测试指南

---

## 📋 当前状态

**数据库**：✅ 迁移完成
**后端代码**：✅ 开发完成，待编译
**前端代码**：✅ 开发完成，待启动
**集成工作**：✅ 核心组件完成，Chat.vue 需按指南集成

---

## 🛠️ 接下来的步骤

### 第一步：安装 Maven

#### macOS
```bash
# 使用 Homebrew 安装
brew install maven

# 验证安装
mvn -version
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt-get install maven

# CentOS/RHEL
sudo yum install maven
```

#### Windows
1. 下载 Maven：https://maven.apache.org/download.cgi
2. 解压到 `C:\Program Files\Apache\Maven`
3. 设置环境变量：
   ```
   MAVEN_HOME=C:\Program Files\Apache\Maven
   PATH=%PATH%;%MAVEN_HOME%\bin
   ```

### 第二步：编译后端项目

```bash
# 进入后端目录
cd /Users/nickxiao/JasEatsChoice/JasEatsChoiceJava

# 清理并编译（首次运行可能较慢，需要下载依赖）
mvn clean package -DskipTests

# 编译成功后会在 target 目录生成 jar 文件
ls -lh target/*.jar
```

**如果编译遇到问题**：

```bash
# 1. 清理缓存
mvn clean

# 2. 跳过测试编译
mvn package -DskipTests -Dmaven.test.skip=true

# 3. 强制更新依赖
mvn clean install -DskipTests -U
```

### 第三步：启动后端服务

```bash
# 方式1：使用 Maven 启动（推荐用于开发）
cd /Users/nickxiao/JasEatsChoice/JasEatsChoiceJava
mvn spring-boot:run

# 方式2：使用 jar 文件启动（推荐用于生产）
java -jar target/jaseatschoicejava-1.0.0.jar

# 方式3：后台运行
nohup java -jar target/jaseatschoicejava-1.0.0.jar > /tmp/app.log 2>&1 &
```

**验证后端启动成功**：
- 看到 "Started JaseatschoicejavaApplication" 消息
- 访问 http://localhost:8080/ 检查服务状态

### 第四步：启动前端服务

```bash
# 新开一个终端窗口

# 进入前端目录
cd /Users/nickxiao/JasEatsChoice/JasEatsChoiceFront

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev

# 或
npm run serve
```

**验证前端启动成功**：
- 看到 "Local: http://localhost:5173/" 消息
- 浏览器访问 http://localhost:5173/

### 第五步：集成 Chat.vue（重要！）

后端和前端都启动后，还需要按指南集成 Chat.vue：

**请查看**：
- 📄 [CHAT_INTEGRATION_GUIDE.md](JasEatsChoiceFront/CHAT_INTEGRATION_GUIDE.md)

这个文档包含详细的集成步骤和代码示例。

**核心步骤**（5-10分钟）：
1. 在 Chat.vue 中引入加菜组件
2. 添加状态变量
3. 添加方法函数
4. 在 GroupOrderDrawer 上绑定新事件

---

## 🧪 功能测试

### 测试场景一：用户发起加菜

1. 登录系统
2. 进入一个有群订单的群聊
3. 打开群订单抽屉
4. 点击"我要加菜"按钮
5. 选择菜品并提交
6. **预期结果**：提示"加菜请求已提交"

### 测试场景二：发起者审核

1. 切换到群订单发起者账号
2. 打开群订单抽屉
3. 点击"查看审核"按钮
4. 勾选加菜请求
5. 点击"批量通过"
6. **预期结果**：提示"已通过 X 个加菜请求"

### API 端点验证

```bash
# 测试加菜设置 API
curl -X GET http://localhost:8080/v1/add-dish/setting/1

# 测试获取审核列表 API
curl -X GET http://localhost:8080/v1/add-dish/review-list/1

# 测试创建加菜请求 API（需要 token）
curl -X POST http://localhost:8080/v1/add-dish/request \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "groupOrderId": 1,
    "dishItems": [{"dishId": 1, "quantity": 2}]
  }'
```

---

## ❓ 常见问题

### Q1: Maven 编译失败
**A**: 检查 Java 版本（需要 JDK 17+）
```bash
java -version
# 应该显示 17.x.x 或更高版本
```

### Q2: 端口被占用
**A**: 修改端口号或杀死占用进程
```bash
# 查看占用 8080 端口的进程
lsof -i :8080
kill -9 <PID>

# 或修改 application.yml 中的端口
server.port=8081
```

### Q3: 数据库连接失败
**A**: 检查数据库配置
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jia_shi_yi_xuan
    username: root
    password: 123456
```

### Q4: 前端无法调用后端 API
**A**: 检查跨域配置和 API 地址
```javascript
// 前端 config/index.js
export default {
  baseURL: 'http://localhost:8080'
}
```

---

## 📚 相关文档

| 文档 | 路径 | 说明 |
|-----|------|------|
| 实施总结 | [加菜功能实施总结.md](加菜功能实施总结.md) | 完整功能说明 |
| Chat 集成指南 | [CHAT_INTEGRATION_GUIDE.md](JasEatsChoiceFront/CHAT_INTEGRATION_GUIDE.md) | 集成步骤 |
| 部署测试指南 | [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) | 详细部署步骤 |
| 数据库迁移脚本 | [migration_add_dish_feature.sql](migration_add_dish_feature.sql) | SQL 脚本 |

---

## 🎯 快速测试（如果 Maven 已安装）

```bash
# 终端1：启动后端
cd /Users/nickxiao/JasEatsChoice/JasEatsChoiceJava
mvn spring-boot:run

# 终端2：启动前端
cd /Users/nickxiao/JasEatsChoice/JasEatsChoiceFront
npm run dev

# 浏览器访问
open http://localhost:5173/
```

---

## ✅ 完成检查清单

在完成所有步骤后，请验证：

- [ ] Maven 安装成功（`mvn -version`）
- [ ] 后端编译成功（`target/*.jar` 文件存在）
- [ ] 后端服务启动（`http://localhost:8080` 可访问）
- [ ] 前端服务启动（`http://localhost:5173` 可访问）
- [ ] 数据库表已创建（`SHOW TABLES LIKE 't_add_dish%'`）
- [ ] Chat.vue 已按指南集成
- [ ] 加菜功能可正常使用

---

## 📞 需要帮助？

如果遇到问题，请：

1. **查看日志**：
   - 后端日志：控制台输出或 `app.log`
   - 前端日志：浏览器控制台（F12）

2. **参考文档**：
   - 部署测试指南：[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
   - 集成指南：[CHAT_INTEGRATION_GUIDE.md](JasEatsChoiceFront/CHAT_INTEGRATION_GUIDE.md)

3. **检查数据**：
   - 数据库：`mysql -u root -p123456 -e "USE jia_shi_yi_xuan; SHOW TABLES;"`
   - API 测试：使用 curl 或 Postman

---

**祝你成功部署！** 🎉
