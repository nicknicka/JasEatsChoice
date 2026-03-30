# Emoji占位符使用说明

## ✅ 已完成的修改

### 1. **DishListCard.vue** - 菜品列表卡片

**修改位置**：第28-36行

**修改前**：
```vue
<image
    :src="dish.imageUrl || '/static/images/placeholder-dish.png'"
    mode="aspectFill"
    class="dish-img"
    @error="handleImageError"
/>
```

**修改后**：
```vue
<image
    v-if="dish.imageUrl"
    :src="dish.imageUrl"
    mode="aspectFill"
    class="dish-img"
    @error="handleImageError"
/>
<text v-else class="placeholder-icon">🍲</text>
```

**效果**：
- ✅ 有图片时：显示实际图片
- ✅ 无图片时：显示🍲 emoji占位符

---

### 2. **FavoriteListCard.vue** - 收藏列表卡片

**修改位置**：第30-37行

**修改前**：
```vue
<image
    :src="item.imageUrl || item.image || '/static/images/placeholder-dish.png'"
    mode="aspectFill"
    class="favorite-img"
    @error="handleImageError"
/>
```

**修改后**：
```vue
<image
    v-if="item.imageUrl || item.image"
    :src="(item.imageUrl || item.image)"
    mode="aspectFill"
    class="favorite-img"
    @error="handleImageError"
/>
<text v-else class="placeholder-icon">🍲</text>
```

**效果**：
- ✅ 有图片时：显示实际图片
- ✅ 无图片时：显示🍲 emoji占位符

---

### 3. **UserInfoCard.vue** - 用户信息卡片

**修改位置**：第24-31行

**修改前**：
```vue
<image
    :src="data.avatar || '/static/images/default-avatar.png'"
    mode="aspectFill"
    class="user-avatar"
    @error="handleAvatarError"
/>
```

**修改后**：
```vue
<image
    v-if="data.avatar"
    :src="data.avatar"
    mode="aspectFill"
    class="user-avatar"
    @error="handleAvatarError"
/>
<text v-else class="placeholder-icon">👤</text>
```

**效果**：
- ✅ 有头像时：显示实际头像
- ✅ 无头像时：显示👤 emoji占位符

---

## 🎨 添加的样式

### 占位符样式（添加到所有卡片组件）

```scss
.placeholder-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    font-size: 80rpx;
    opacity: 0.3;
}
```

**样式特点**：
- ✅ 居中显示
- ✅ 大小适中（80rpx）
- ✅ 半透明（opacity: 0.3）
- ✅ 不干扰实际内容

---

## 📋 使用的Emoji清单

| 卡片类型 | Emoji | 说明 |
|---------|-------|------|
| **DishListCard** | 🍲 | 菜品占位符 |
| **FavoriteListCard** | 🍲 | 菜品占位符 |
| **UserInfoCard** | 👤 | 用户头像占位符 |

---

## 🎯 优势

### 使用Emoji的优势

1. ✅ **无需额外文件**：不需要创建图片文件
2. ✅ **加载速度快**：Emoji是系统字体，无需加载
3. ✅ **样式统一**：所有emoji风格一致
4. ✅ **适配性好**：所有平台都支持
5. ✅ **可扩展**：将来可以轻松替换为图片

### 与图片占位符对比

| 特性 | Emoji占位符 | 图片占位符 |
|------|------------|-----------|
| **文件大小** | 0KB | ~10-50KB |
| **加载速度** | 瞬间显示 | 需要加载 |
| **网络请求** | 无 | 需要 |
| **维护成本** | 无 | 需要维护 |
| **可定制性** | 较低 | 高 |

---

## 🧪 测试方法

### 测试1：无图片数据

**发送消息**：
```
推荐一些好吃的菜品
```

**后端返回**（无imageUrl）：
```json
{
  "dishes": [
    {
      "dishId": "1",
      "dishName": "西红柿炒鸡蛋",
      "price": 18.00
      // 没有imageUrl字段
    }
  ]
}
```

**预期显示**：
- ✅ 显示🍲 emoji占位符
- ✅ 菜品名称、价格正常显示

---

### 测试2：有图片数据

**后端返回**（有imageUrl）：
```json
{
  "dishes": [
    {
      "dishId": "1",
      "dishName": "西红柿炒鸡蛋",
      "imageUrl": "https://example.com/dish.jpg",
      "price": 18.00
    }
  ]
}
```

**预期显示**：
- ✅ 显示实际图片
- ✅ 图片加载失败时自动降级到emoji

---

### 测试3：用户信息无头像

**发送消息**：
```
我的个人信息
```

**后端返回**（无avatar）：
```json
{
  "userId": "1",
  "username": "张三"
  // 没有avatar字段
}
```

**预期显示**：
- ✅ 显示👤 emoji占位符
- ✅ 用户名、ID正常显示

---

## 📝 注意事项

1. **@error事件保留**：
   - 虽然使用了v-if条件，但保留了@error事件
   - 这样可以处理图片加载失败的情况
   - 未来可以扩展显示错误提示

2. **条件判断**：
   - 使用`v-if`判断图片是否存在
   - 不存在时直接显示emoji
   - 不需要等待图片加载失败

3. **样式一致性**：
   - 所有emoji占位符样式统一
   - 大小、透明度一致
   - 居中对齐

---

## 🔮 未来扩展

### 如果将来需要使用实际图片

1. **创建图片文件**：
   ```bash
   mkdir -p static/images
   # 添加 placeholder-dish.png 和 default-avatar.png
   ```

2. **修改代码**：
   ```vue
   <!-- 将emoji改为图片 -->
   <image
     v-else
     src="/static/images/placeholder-dish.png"
     class="placeholder-img"
   />
   ```

3. **修改样式**：
   ```scss
   .placeholder-img {
       @extend .dish-img;
       opacity: 0.3;
   }
   ```

---

## ✅ 总结

**已完成**：
- ✅ 3个卡片组件已修改为使用emoji占位符
- ✅ 添加了统一的占位符样式
- ✅ 保留了错误处理机制

**效果**：
- ✅ 无需额外图片文件
- ✅ 加载速度更快
- ✅ 用户体验更好

**下一步**：
- 🧪 测试卡片显示效果
- 🎨 可选：调整emoji大小或透明度
- 📝 可选：添加更多emoji选项

---

**修改完成日期**：2026-03-30
**修改人**：Claude Code
