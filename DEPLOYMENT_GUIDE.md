# 加菜功能 - 测试与部署完整指南

**文档版本**：1.0
**创建日期**：2026-01-24
**适用版本**：佳食宜选 v1.0

---

## 📋 目录

1. [数据库迁移](#一数据库迁移)
2. [后端编译与启动](#二后端编译与启动)
3. [前端启动](#三前端启动)
4. [功能测试](#四功能测试)
5. [常见问题排查](#五常见问题排查)
6. [部署上线](#六部署上线)

---

## 一、数据库迁移

### 1.1 备份数据库（重要！）

在执行迁移前，请务必备份数据库：

```bash
# 方式1：使用 mysqldump 备份
mysqldump -u root -p jia_shi_yi_xuan > backup_before_add_dish_$(date +%Y%m%d_%H%M%S).sql

# 方式2：在 MySQL 中备份
mysql -u root -p
USE jia_shi_yi_xuan;
CREATE TABLE t_order_backup AS SELECT * FROM t_order;
CREATE TABLE t_group_order_backup AS SELECT * FROM t_group_order;
CREATE TABLE t_order_dish_backup AS SELECT * FROM t_order_dish;
```

### 1.2 执行迁移脚本

```bash
# 进入项目目录
cd /Users/nickxiao/JasEatsChoice

# 执行迁移脚本
mysql -u root -p jia_shi_yi_xuan < migration_add_dish_feature.sql
```

### 1.3 验证迁移结果

```bash
# 连接到数据库
mysql -u root -p jia_shi_yi_xuan

# 查看新增的表
SHOW TABLES LIKE 't_add_dish%';

# 查看表结构
DESC t_add_dish_request;
DESC t_add_dish_setting;

# 查看修改后的表结构
DESC t_group_order;
DESC t_order;
DESC t_order_dish;
```

### 1.4 初始化默认数据

```sql
-- 为现有群订单创建默认加菜设置
INSERT INTO t_add_dish_setting (group_order_id, add_dish_permission, budget_limit, max_dish_count)
SELECT id, 0, NULL, NULL
FROM t_group_order
WHERE id NOT IN (SELECT group_order_id FROM t_add_dish_setting);
```

**预期输出**：
```
Query OK, 3 rows affected (假设有3个群订单)
```

---

## 二、后端编译与启动

### 2.1 检查后端依赖

```bash
cd JasEatsChoiceJava

# 检查 pom.xml 是否存在
ls -la pom.xml

# 检查 Java 版本
java -version
# 需要 Java 17 或更高版本
```

### 2.2 清理并编译项目

```bash
# 清理旧的编译文件
mvn clean

# 编译项目（跳过测试）
mvn package -DskipTests

# 编译成功后，target 目录下会生成 jar 文件
ls -lh target/*.jar
```

**预期输出**：
```
-rw-r--r--  1 user  staff   XXM Jan 24 12:00 jaseatschoicejava-1.0.0.jar
```

### 2.3 检查编译错误

如果编译出现错误，检查以下几点：

1. **缺少依赖**：
   ```bash
   mvn dependency:resolve
   ```

2. **代码错误**：查看错误信息并修复
   - 常见错误：缺少 @Data 注解、Mapper.xml 文件缺失等

3. **重新编译**：
   ```bash
   mvn clean package -DskipTests -U
   ```

### 2.4 启动后端服务

```bash
# 方式1：使用 Maven 启动（开发模式）
mvn spring-boot:run

# 方式2：使用 jar 文件启动（生产模式）
java -jar target/jaseatschoicejava-1.0.0.jar

# 方式3：后台运行
nohup java -jar target/jaseatschoicejava-1.0.0.jar > app.log 2>&1 &
```

**预期输出**：
```
  ____  __  __ _
 |  _ \/ _| |/ / |
 | |_) | |   / ____|  ____
 |  _ <| |   \___ \ / _  |
 |_| \_\|_|\_____| \___/  v2.7.18

2026-01-24 12:00:00.000  INFO 12345 --- [  restartedMain] c.j.jaseatschoicejava.JaseatschoicejavaApplication : Starting JaseatschoicejavaApplication v1.0.0
2026-01-24 12:00:00.500  INFO 12345 --- [  restartedMain] c.j.jaseatschoicejava.JaseatschoicejavaApplication : The following profiles are active: dev
...
2026-01-24 12:00:05.000  INFO 12345 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
2026-01-24 12:00:05.500  INFO 12345 --- [  restartedMain] c.j.jaseatschoicejava.JaseatschoicejavaApplication : Started JaseatschoicejavaApplication in 5.5 seconds
```

### 2.5 验证后端服务

```bash
# 检查服务是否启动
curl -X GET http://localhost:8080/v1/health

# 或者检查加菜 API 端点
curl -X GET http://localhost:8080/v1/add-dish/setting/1
```

---

## 三、前端启动

### 3.1 检查前端依赖

```bash
cd ../JasEatsChoiceFront

# 检查 node 和 npm 版本
node -v
npm -v

# 如果需要安装依赖
npm install
```

### 3.2 启动前端服务

```bash
# 开发模式
npm run dev

# 或者
npm run serve
```

**预期输出**：
```
  VITE v4.5.0  ready in 1234 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h to show help
```

### 3.3 访问前端界面

打开浏览器，访问：
```
http://localhost:5173/
```

登录后，进入聊天界面。

---

## 四、功能测试

### 4.1 测试场景一：用户发起加菜（基础流程）

**步骤**：

1. **进入群聊**：选择一个群订单进行中的群聊
2. **打开群订单抽屉**：点击群订单按钮，打开订单详情
3. **查看加菜入口**：确认看到"加菜功能"区域
   - ✅ 看到"我要加菜"按钮
4. **点击"我要加菜"**：
   - ✅ 打开加菜对话框
   - ✅ 显示已点菜品清单
   - ✅ 显示可用菜品列表
5. **选择菜品**：
   - ✅ 可以点击"添加"按钮选择菜品
   - ✅ 可以调整数量
   - ✅ 显示累计金额
6. **提交加菜请求**：
   - ✅ 点击"提交加菜请求"
   - ✅ 收到成功提示"加菜请求已提交，等待发起者审核"

**API请求验证**：
```bash
# 查看后端日志
tail -f app.log | grep "加菜"

# 或在浏览器 Network 查看请求
POST /v1/add-dish/request
```

### 4.2 测试场景二：发起者审核加菜

**步骤**：

1. **切换到发起者账号**
2. **打开群订单抽屉**
3. **查看审核按钮**：
   - ✅ 看到"查看审核"按钮
   - ✅ 显示待审核数量徽章（如"3"）
4. **点击"查看审核"**：
   - ✅ 打开审核面板
   - ✅ 显示待审核列表（按时间倒序）
   - ✅ 显示加菜人、菜品、金额
5. **批量审核**：
   - ✅ 可以勾选单个或多个请求
   - ✅ 点击"批量通过"
   - ✅ 收到成功提示
6. **查看审核结果**：
   - ✅ 已审核的请求从列表中消失
   - ✅ 待审核数量徽章更新

### 4.3 测试场景三：超时自动驳回

**步骤**：

1. **提交加菜请求**
2. **等待15分钟**（或修改数据库中的 timeout_time）
3. **运行定时任务**：
   ```bash
   # 定时任务每2分钟自动运行
   # 查看日志确认超时处理
   tail -f app.log | grep "超时"
   ```
4. **验证结果**：
   - ✅ 15分钟后请求状态变为"超时驳回"
   - ✅ 加菜用户收到通知

### 4.4 测试场景四：权限控制

**测试点**：

1. **非发起者权限**：
   - ✅ 非发起者看不到"查看审核"按钮
   - ✅ 可以看到"我要加菜"按钮
2. **发起者权限**：
   - ✅ 可以看到"查看审核"按钮
   - ✅ 可以看到"我要加菜"按钮

### 4.5 测试场景五：撤回请求

**步骤**：

1. **提交加菜请求**
2. **在审核前撤回**：
   - ✅ 可以点击"撤回"按钮
   - ✅ 收到"已撤回"提示
   - ✅ 请求状态更新为"已撤回"

### 4.6 数据库验证

在测试过程中或测试后，验证数据库数据：

```sql
-- 查看加菜请求
SELECT
    id,
    group_order_id,
    request_user_id,
    approval_status,
    total_amount,
    create_time
FROM t_add_dish_request
ORDER BY create_time DESC
LIMIT 10;

-- 查看审核状态分布
SELECT
    approval_status,
    COUNT(*) as count
FROM t_add_dish_request
GROUP BY approval_status;

-- 查看待审核数量
SELECT COUNT(*)
FROM t_add_dish_request
WHERE approval_status = 0;
```

---

## 五、常见问题排查

### 5.1 数据库迁移失败

**问题**：迁移脚本执行报错

**排查步骤**：
```bash
# 1. 检查数据库连接
mysql -u root -p -e "SELECT VERSION();"

# 2. 检查数据库是否存在
mysql -u root -p -e "SHOW DATABASES LIKE 'jia_shi_yi_xuan';"

# 3. 检查现有表
mysql -u root -p jia_shi_yi_xuan -e "SHOW TABLES;"

# 4. 查看详细错误
mysql -u root -p jia_shi_yi_xuan < migration_add_dish_feature.sql 2>&1 | tee migration.log
cat migration.log
```

**常见错误和解决方案**：

| 错误信息 | 原因 | 解决方案 |
|---------|------|----------|
| Duplicate column name | 列已存在 | 检查表结构，删除重复的 ALTER TABLE 语句 |
| Table already exists | 表已存在 | 在脚本开头添加 DROP TABLE 语句 |
| Foreign key constraint fails | 外键约束 | 先删除外键，再修改表 |

### 5.2 后端启动失败

**问题**：后端服务启动失败

**排查步骤**：

1. **查看日志**：
   ```bash
   # 查看启动日志
   cat app.log | tail -100

   # 或实时查看
   tail -f app.log
   ```

2. **常见错误**：

   **a) 端口被占用**
   ```
   Error: Port 8080 was already in use.
   ```
   **解决方案**：
   ```bash
   # 查找占用端口的进程
   lsof -i :8080

   # 杀死进程
   kill -9 <PID>

   # 或修改配置文件中的端口
   ```

   **b) 数据库连接失败**
   ```
   Error: Could not open connection to database
   ```
   **解决方案**：
   ```properties
   # 检查 application.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/jia_shi_yi_xuan
       username: root
       password: 123456
   ```

   **c) 类找不到**
   ```
   Error: ClassNotFoundException
   ```
   **解决方案**：
   ```bash
   mvn clean compile
   mvn package -DskipTests
   ```

### 5.3 前端启动失败

**问题**：前端服务无法启动

**排查步骤**：

1. **检查 Node 版本**：
   ```bash
   node -v  # 需要 v16 或更高
   npm -v
   ```

2. **清除缓存**：
   ```bash
   rm -rf node_modules
   rm package-lock.json
   npm install
   ```

3. **检查端口**：
   ```bash
   # 查看端口占用
   lsof -i :5173
   ```

### 5.4 API 调用失败

**问题**：前端调用后端 API 失败

**排查步骤**：

1. **检查后端服务**：
   ```bash
   curl http://localhost:8080/v1/add-dish/setting/1
   ```

2. **检查跨域配置**：
   ```java
   // 后端需要添加 CORS 配置
   @CrossOrigin(origins = "*")
   ```

3. **检查前端 API 配置**：
   ```javascript
   // config/index.js 或 api.js
   const API_CONFIG = {
     baseURL: 'http://localhost:8080'
   }
   ```

4. **查看浏览器控制台**：
   - 打开开发者工具（F12）
   - 查看 Console 和 Network 标签
   - 检查请求状态码和响应内容

### 5.5 加菜功能不显示

**问题**：加菜按钮不显示

**排查步骤**：

1. **检查 groupOrder 数据**：
   ```javascript
   console.log('群订单数据:', currentGroupOrder.value)
   console.log('是否有商家:', currentGroupOrder.value?.merchantName)
   console.log('订单状态:', currentGroupOrder.value?.status)
   ```

2. **检查条件渲染**：
   ```vue
   <!-- 确保条件正确 -->
   v-if="groupOrder.status === 'active' && hasMerchant"
   ```

3. **检查组件引入**：
   ```javascript
   // 确保正确引入 GroupOrderDrawer 组件
   import GroupOrderDrawer from '...'
   ```

---

## 六、部署上线

### 6.1 生产环境配置

#### 后端配置修改

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://your-production-host:3306/jia_shi_yi_xuan
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  jpa:
    hibernate:
      ddl-auto: none  # 生产环境不要自动建表

logging:
  level:
    root: INFO
    com.xx.jaseatschoicejava: WARN
  file:
    name: /var/log/jaseatschoice/application.log
```

#### 前端配置修改

```javascript
// .env.production
VITE_API_BASE_URL=https://api.yourdomain.com
```

### 6.2 后端部署

#### 方式1：使用 Docker（推荐）

```dockerfile
# Dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/jaseatschoicejava-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# 构建镜像
docker build -t jaseatschoice:latest .

# 运行容器
docker run -d \
  --name jaseatschoice \
  -p 8080:8080 \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=123456 \
  jaseatschoice:latest
```

#### 方式2：使用 systemd

```bash
# 创建服务文件
sudo vim /etc/systemd/system/jaseatschoice.service
```

```ini
[Unit]
Description=JasEatsChoice Application
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/jaseatschoice
ExecStart=/usr/bin/java -jar /opt/jaseatschoice/jaseatschoicejava-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 启动服务
sudo systemctl daemon-reload
sudo systemctl start jaseatschoice
sudo systemctl enable jaseatschoice
```

### 6.3 前端部署

#### 方式1：Nginx 部署

```bash
# 1. 构建前端
npm run build

# 2. 配置 Nginx
sudo vim /etc/nginx/sites-available/jaseatschoice
```

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    root /var/www/jaseatschoice;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
# 3. 启用配置
sudo ln -s /etc/nginx/sites-available/jaseatschoice /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 6.4 监控和日志

#### 后端监控

```bash
# 查看日志
tail -f /var/log/jaseatschoice/application.log

# 使用 PM2 监控 Node 进程（如果适用）
pm2 start jaseatschoice --name "backend"
pm2 logs backend
```

#### 前端监控

```bash
# Nginx 访问日志
tail -f /var/log/nginx/access.log

# 错误日志
tail -f /var/log/nginx/error.log
```

---

## 七、性能优化建议

### 7.1 数据库优化

```sql
-- 添加索引
CREATE INDEX idx_add_dish_timeout ON t_add_dish_request(timeout_time, approval_status);
CREATE INDEX idx_add_dish_user ON t_add_dish_request(request_user_id, create_time DESC);

-- 定期清理历史数据
DELETE FROM t_add_dish_request
WHERE approval_status IN (2, 3, 4)
AND create_time < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

### 7.2 缓存策略

```java
// 使用 Redis 缓存群订单设置
@Cacheable(value = "addDishSetting", key = "#groupOrderId")
public AddDishSettingDTO getSetting(Long groupOrderId) {
    // ...
}
```

### 7.3 异步处理

```java
// 异步处理加菜通知
@Async
public void notifyAddDishRequest(AddDishRequest request) {
    // 发送 WebSocket 通知
}
```

---

## 八、检查清单

部署前检查清单：

- [ ] 数据库备份完成
- [ ] 数据库迁移脚本执行成功
- [ ] 新表创建成功（t_add_dish_request、t_add_dish_setting）
- [ ] 现有表修改成功
- [ ] 后端编译成功，无错误
- [ ] 后端服务正常启动
- [ ] 后端 API 可以访问
- [ ] 前端依赖安装完成
- [ ] 前端服务正常启动
- [ ] 前端可以正常访问
- [ ] 加菜功能集成完成
- [ ] 功能测试通过

---

## 九、联系支持

如果遇到问题，请查看：

1. **错误日志**：
   - 后端：`/var/log/jaseatschoice/application.log`
   - 前端：浏览器控制台

2. **文档参考**：
   - [加菜功能实施总结.md](加菜功能实施总结.md)
   - [未实现功能清单.md](未实现功能清单.md)

3. **GitHub Issues**：
   - 提交问题并附上错误日志

---

**祝部署顺利！** 🎉
