# 构建修复说明

## 问题描述

UniApp 项目的 H5 构建失败，错误信息：
```
"isInSSRComponentSetup" is not exported by "node_modules/vue/dist/vue.runtime.esm-bundler.js"
```

## 根本原因

- **@dcloudio/uni-app** 使用了 alpha 版本（3.0.0-alpha-5000720260327001，2026-03-27）
- 该版本错误地依赖 Vue 内部函数：`injectHook` 和 `isInSSRComponentSetup`
- 这些函数在 Vue 3.5.13 中未导出到公共 API
- 导致构建时无法解析这些导入

## 解决方案

### 1. 版本锁定

将所有 Vue 相关包锁定到 3.5.13 版本（在 package.json 中）：

```json
{
  "dependencies": {
    "vue": "3.5.13"
  },
  "devDependencies": {
    "@vue/compiler-sfc": "3.5.13"
  },
  "overrides": {
    "@vue/shared": "3.5.13",
    "@vue/runtime-core": "3.5.13",
    "@vue/runtime-dom": "3.5.13",
    "@vue/compiler-core": "3.5.13",
    "@vue/compiler-dom": "3.5.13",
    "@vue/compiler-sfc": "3.5.13",
    "@vue/reactivity": "3.5.13",
    "@vue/server-renderer": "3.5.13",
    "vue": "3.5.13"
  }
}
```

### 2. 自动修复脚本

创建了 `scripts/fix-dependencies.js` 脚本，在每次 `npm install` 后自动运行：

1. **修复 runtime-core**：在导出列表中添加 `injectHook` 和 `isInSSRComponentSetup`
2. **修复 uni-app**：移除对 `isInSSRComponentSetup` 的使用（H5 模式不需要 SSR 检查）

### 3. 配置文件位置

UniApp 构建工具要求配置文件在 `src/` 目录下：
- `src/manifest.json`
- `src/pages.json`

## 使用方法

### 首次安装或重新安装依赖

```bash
npm install
```

postinstall 脚本会自动运行并修复依赖兼容性问题。

### 构建 H5 版本

```basy
npm run build:h5
```

构建输出在 `dist/build/h5/` 目录。

### 构建微信小程序版本

```bash
npm run build:mp-weixin
```

## 注意事项

1. **不要手动修改 node_modules**：所有修复通过 postinstall 脚本自动完成
2. **版本锁定**：保持 Vue 3.5.13，不要升级到 3.5.x（可能会有兼容性问题）
3. **alpha 版本警告**：@dcloudio/uni-app 的 alpha 版本可能不稳定，建议等待稳定版本

## 构建警告

构建过程中可能会出现以下警告，可以忽略：

- **Sass deprecation warnings**：使用了已弃用的 Sass API
- **legacy-js-api**：Sass 旧版 JS API 将在 Dart Sass 2.0 中移除

这些警告不影响构建结果。

## 技术细节

### 修复的文件

1. `node_modules/@vue/runtime-core/dist/runtime-core.esm-bundler.js`
   - 添加导出：`injectHook, isInSSRComponentSetup`

2. `node_modules/@dcloudio/uni-app/dist/uni-app.es.js`
   - 移除导入：`isInSSRComponentSetup`
   - 简化逻辑：直接调用 `injectHook` 而不检查 SSR 环境

### 为什么这个方案可行

- **H5 模式不需要 SSR**：UniApp 的 H5 构建不使用服务端渲染
- **函数功能**：`isInSSRComponentSetup` 只是条件判断，在 H5 中始终返回 false
- **向后兼容**：修复不影响小程序等其他平台的构建

## 后续建议

1. **关注 @dcloudio/uni-app 更新**：等待修复此问题的稳定版本
2. **测试所有平台**：确保修复不影响微信小程序、App 等平台
3. **考虑降级**：如果问题持续，可以考虑使用非 alpha 版本的 @dcloudio 包

## 相关资源

- [Vue 3.5 发布说明](https://blog.vuejs.org/posts/vue-3-5)
- [UniApp 官方文档](https://uniapp.dcloud.net.cn/)
- [Vite 构建工具](https://vitejs.dev/)
