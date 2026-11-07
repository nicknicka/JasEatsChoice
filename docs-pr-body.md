# #341 admin-merchants-audit 设计对齐改造

## 🎯 目标
将商家审核页面统一到 DESIGN-BASELINE 标准，实现 merchant/admin 四端 UI 一致性。

---

## ✅ 核心改造

### 1. 页面标题层
**Before**:  
- 自定义 `.page-header` div (h1 + subtitle)  
- 硬编码样式 (`font-size: 1.714rem`, `color: #303133`)  
- 无返回导航栏集成  

**After**:  
- AppPageHeader(title="商家审核", :compact="true")  
- 自动集成返回按钮与紧凑密度规范  
- 符合 §4.2 页头语言：merchant/admin 统一用紧凑版

---

### 2. KPI 概览卡
**Before**:  
- `<el-statistic>` 包裹在 `<el-card>` 中 ×3  
- 渐变图标块（不符合规范）  
- 间距裸值 (`gap: 16px`, `margin-bottom: 20px`)  

**After**:  
- AppStatCard(label, value, icon) ×3  
- 浅底 + 线性图标 (Clock, Checked, TrendCharts)  
- Tabular-nums 数值格式 + CSS 变量间距  
- 符合 §5.5 概览卡/KPI：结构固定、色调收敛

---

### 3. 样式 Tokenization
**移除**:  
- ❌ 裸色值 (`#303133`, `#909399`)  
- ❌ 像素值 (`20px`, `16px`, `24px`)  
- ❌ 注释痕迹 (`/* 原值：Xpx */`)  

**新增**:  
```less
@import '@/assets/css/nordic-theme.less';

.merchant-audit-container {
  padding: var(--card-padding-compact) var(--page-gutter);
  background: var(--canvas);
}
.stats-section {
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--card-gap-compact);
  margin-bottom: var(--space-8);
}
```
- 符合 §7.1 唯一颜色源 tokens.css  
- 全站颜色/间距只有一份来源

---

## 📁 落地文件

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| MerchantAudit.vue | 重写 | 模板 + 脚本 + 样式标准化 |
| components/App*.vue | 复制 | 组件依赖同步到 worktree |
| assets/css/*.less | 复制 | 主题 token 文件同步 |

---

## ✅ 验证证据

- [x] **Build**: ✓ built in 6.86s (npm run build:web)
- [ ] **ESLint**: Baseline 维持 125 errors (待运行校验)
- [ ] **Visual**: 需浏览器截图验证 AppPageHeader/AppStatCard 渲染
- [ ] **Functional**: Playwright specs/01.js smoke test 待跑

---

## 🔄 与设计基线对齐点

| 条款 | 符合度 | 验证 |
|------|--------|------|
| §4.2 页头语言 | ✅ | AppPageHeader(compact) |
| §5.5 概览卡/KPI | ✅ | 浅底 + 线性图标 + tabular-nums |
| §7.1 颜色源 | ✅ | @import tokens.css, no hardcoded colors |
| §3.5 装饰性动画 | ✅ | 无进场动画 |

---

## ⚠️ 注意事项

1. Worktree 需完整复制 components/common 子目录及 assets/css 主题文件，否则构建会报 ENOENT。
2. CSS import 路径必须用 `@/assets/...` 别名形式，相对路径 `../../../` 在 vite resolve 下解析失败。
3. ESLint baseline 当前为 125 errors，PR 不得增加 error count。

---

## 📈 后续建议

合并此 PR 后立即启动 #342 admin-orders (Order Management Core)，复用相同 pattern：
- AppPageHeader(compact)
- AppStatCard×N (KPI cards)
- el-table(StatusTag)
- CSS tokenization

预计每页改造耗时 <15 分钟，按每日推进 2~3 个 admin 页面，P0 Core Audit Flow 可在 1 天内收尾。
