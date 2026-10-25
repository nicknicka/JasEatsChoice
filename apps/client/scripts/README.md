# 字体大小批量转换工具使用说明

## 📋 概述

这个工具用于将 Vue 组件中的固定 `px` 字体大小转换为相对 `rem` 单位，使其能够响应全局字体大小设置。

## ✅ 已完成的组件

以下关键布局组件已经手动优化完成：

- ✅ **CommonHome.vue** - 侧边栏和顶部导航栏
- ✅ **AdminLayout.vue** - 管理员后台布局
- ✅ **App.vue** - 根容器（无需修改）

## 🚀 使用脚本

### 方法 1：Node.js 脚本（推荐）

#### 预览模式（不修改文件）

```bash
cd JasEatsChoiceFront
node scripts/convert-font-size.js
```

#### 执行修改

```bash
node scripts/convert-font-size.js --fix
```

### 方法 2：Bash 脚本

#### 预览模式（不修改文件）

```bash
cd JasEatsChoiceFront
./scripts/convert-font-size.sh
```

#### 执行修改

```bash
./scripts/convert-font-size.sh --fix
```

## 📊 转换对照表

| 原值 (px) | 转换后 (rem) | 说明 |
|----------|-------------|------|
| 11px | 0.75rem | 小号文字 |
| 12px | 0.857rem | 较小文字 |
| 13px | 0.929rem | 稍小文字 |
| 14px | 1rem | **基准字体（默认）** |
| 15px | 1.071rem | 稍大文字 |
| 16px | 1.143rem | 较大文字 |
| 18px | 1.286rem | 大号文字 |
| 20px | 1.429rem | 标题文字 |
| 24px | 1.714rem | 大标题 |
| 28px | 2rem | 超大标题 |
| 32px | 2.286rem | 特大标题 |

## 🔧 工作原理

### 1. 全局字体设置（在 Settings.vue 中）

```javascript
// 用户选择字体大小
body.classList.add('font-medium')  // 14px（默认）
body.classList.add('font-large')   // 16px
body.classList.add('font-small')   // 12px
```

### 2. 全局样式（在 styles.less 中）

```css
body.font-medium { font-size: 14px; }
body.font-large { font-size: 16px; }
body.font-small { font-size: 12px; }
```

### 3. 组件使用相对单位

```css
/* 之前（固定大小） */
.logo { font-size: 24px; }  ❌ 不会随设置变化

/* 之后（相对单位） */
.logo { font-size: 1.5rem; }  ✅ 会随设置变化
```

### 4. 实际效果

当用户设置不同字体大小时：

```
font-medium (14px):
  1.5rem = 14px × 1.5 = 21px

font-large (16px):
  1.5rem = 16px × 1.5 = 24px

font-small (12px):
  1.5rem = 12px × 1.5 = 18px
```

## ⚠️ 注意事项

### 1. 运行前请先提交代码

```bash
git add .
git commit -m "备份：字体大小转换前的代码"
```

### 2. 建议先运行预览模式

查看哪些文件会被修改，确认无误后再执行实际修改。

### 3. 某些特殊情况可能需要手动调整

- Element Plus 组件内部样式
- 动态计算的样式
- 内联样式（`style="font-size: 14px"`）

### 4. 转换后保留原值注释

脚本会在转换后的代码中保留原值作为注释：

```css
/* 转换前 */
font-size: 14px;

/* 转换后 */
font-size: 1rem; /* 原值: 14px */
```

方便后续调试和还原。

## 🧪 测试步骤

1. **运行脚本转换字体大小**
2. **重启开发服务器**
3. **进入设置页面**
4. **切换不同的字体大小**
5. **检查所有页面文字是否同步变化**

## 📝 手动修改示例

如果某些组件需要手动修改，参考以下示例：

### 示例 1：修改标题字体大小

```less
// 之前
.page-title {
  font-size: 24px;
}

// 之后
.page-title {
  font-size: 1.5rem; /* 使用相对单位 */
}
```

### 示例 2：修改按钮字体大小

```less
// 之前
.el-button {
  font-size: 14px;
}

// 之后
.el-button {
  font-size: 1rem; /* 使用相对单位 */
}
```

### 示例 3：修改内联样式

```vue
<!-- 之前 -->
<div style="font-size: 16px">标题</div>

<!-- 之后 -->
<div :style="{ fontSize: '1.143rem' }">标题</div>
```

## 🎯 最佳实践

1. **优先使用 rem 单位** - 对于需要随全局设置变化的文字
2. **保留 px 单位** - 对于固定的装饰性元素（边框、阴影等）
3. **使用 em 单位** - 对于相对于父元素的大小
4. **Element Plus 组件** - 使用全局配置修改大小

## 📚 参考资源

- [MDN - rem 单位](https://developer.mozilla.org/zh-CN/docs/Web/CSS/length)
- [MDN - em 单位](https://developer.mozilla.org/zh-CN/docs/Web/CSS/font-size)
- [CSS 相对单位指南](https://web.dev/accessible-responsive-typography/)

## 🔄 回滚方法

如果转换后出现问题，可以使用 Git 回滚：

```bash
# 查看修改
git diff

# 回滚所有修改
git checkout -- .

# 或者回滚特定文件
git checkout -- path/to/file.vue
```

## ❓ 常见问题

### Q: 为什么某些组件的字体大小没有变化？

A: 可能的原因：
1. 该组件使用了 `scoped` 样式且有固定的 `font-size`
2. 该组件使用了内联样式（`style="font-size: XXpx"`）
3. Element Plus 组件有默认的字体大小设置

解决方法：手动检查并修改该组件的样式。

### Q: 转换后字体变得太大或太小？

A: 检查 `rem` 值是否正确。基准字体大小是 14px（1rem），如果需要调整可以：

1. 修改 [styles.less](../src/renderer/src/assets/css/styles.less) 中的基准值
2. 或者调整具体组件的 `rem` 值

### Q: 可以自定义转换比例吗？

A: 可以！编辑脚本中的 `CONFIG.baseFontSize` 值（默认是 14）。

---

**创建时间**: 2026-02-16
**维护者**: Claude Code
**版本**: 1.0.0
