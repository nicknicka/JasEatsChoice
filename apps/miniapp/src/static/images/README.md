# 占位图片说明

## 需要创建的占位图片

为了确保卡片组件正常显示，需要在 `static/images/` 目录创建以下占位图片：

### 1. 菜品占位图

**路径**：`/static/images/placeholder-dish.png`

**建议**：
- 尺寸：400x400px
- 格式：PNG或JPG
- 内容：菜品图标或"暂无图片"文字
- 背景色：#F5F5F5（浅灰色）

**快速创建**：
```bash
# 在项目根目录执行
mkdir -p static/images
# 添加占位图（请手动创建或使用设计图）
```

---

### 2. 默认头像

**路径**：`/static/images/default-avatar.png`

**建议**：
- 尺寸：200x200px
- 格式：PNG（支持透明）
- 内容：用户轮廓图标
- 背景色：透明

---

## 临时解决方案

如果暂时没有占位图，可以在代码中修改为使用Base64或纯色背景：

### 修改DishListCard.vue

```vue
<image
    :src="dish.imageUrl || ''"
    mode="aspectFill"
    class="dish-img"
    @error="handleImageError"
/>
```

当图片加载失败时，`handleImageError`会设置为占位图路径。

---

## 注意事项

1. **图片路径**：确保static目录在项目根目录
2. **图片大小**：建议不超过50KB，避免加载缓慢
3. **图片格式**：优先使用PNG（支持透明）或JPG（压缩率高）
4. **CDN部署**：生产环境建议使用CDN加速

---

## 创建占位图工具

可以使用以下在线工具快速生成占位图：

1. **Canva** - https://www.canva.com/
   - 搜索"placeholder"模板
   - 自定义尺寸和文字

2. **Placeholder.com** - https://placeholder.com/
   - 在线生成占位图
   - 自定义尺寸和颜色

3. **Figma** - https://www.figma.com/
   - 设计占位图
   - 导出PNG格式

---

**更新日期**：2026-03-30
