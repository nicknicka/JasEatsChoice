/**
 * 修复 UniApp 与 Vue 3.5.13 的兼容性问题
 *
 * 问题：@dcloudio/uni-app alpha 版本依赖 Vue 内部函数（injectHook、isInSSRComponentSetup）
 * 这些函数在 Vue 3.5.13 中未导出，导致构建失败
 *
 * 解决方案：手动在 runtime-core 中添加这些函数的导出
 */

const fs = require('fs');
const path = require('path');

console.log('🔧 修复 UniApp 依赖兼容性问题...');

// 1. 修复 runtime-core.esm-bundler.js - 添加缺失的导出
const runtimeCorePath = path.join(__dirname, '../node_modules/@vue/runtime-core/dist/runtime-core.esm-bundler.js');

if (fs.existsSync(runtimeCorePath)) {
  let content = fs.readFileSync(runtimeCorePath, 'utf8');

  // 检查是否已经修复过
  if (!content.includes('injectHook, isInSSRComponentSetup')) {
    // 在第二个 export 语句中添加缺失的函数
    content = content.replace(
      /export \{ BaseTransition, ([^}]+) \};/,
      (match, p1) => {
        return `export { BaseTransition, ${p1}, injectHook, isInSSRComponentSetup };`;
      }
    );

    fs.writeFileSync(runtimeCorePath, content, 'utf8');
    console.log('✅ runtime-core.esm-bundler.js 已修复');
  } else {
    console.log('✓ runtime-core.esm-bundler.js 已经修复过');
  }
} else {
  console.warn('⚠️  runtime-core.esm-bundler.js 不存在，跳过');
}

// 2. 修复 uni-app.es.js - 移除对 isInSSRComponentSetup 的使用
const uniAppPath = path.join(__dirname, '../node_modules/@dcloudio/uni-app/dist/uni-app.es.js');

if (fs.existsSync(uniAppPath)) {
  let content = fs.readFileSync(uniAppPath, 'utf8');

  // 检查是否已经修复过
  if (content.includes('isInSSRComponentSetup')) {
    // 移除 isInSSRComponentSetup 的导入
    content = content.replace(
      /import \{ shallowRef, ref, getCurrentInstance, isInSSRComponentSetup, injectHook \} from 'vue';/,
      "import { shallowRef, ref, getCurrentInstance, injectHook } from 'vue';"
    );

    // 替换使用：在 H5 构建中，我们假设不在 SSR 环境中
    content = content.replace(
      /!isInSSRComponentSetup && injectHook\(lifecycle, hook, target\);/,
      "injectHook(lifecycle, hook, target);"
    );

    fs.writeFileSync(uniAppPath, content, 'utf8');
    console.log('✅ uni-app.es.js 已修复');
  } else {
    console.log('✓ uni-app.es.js 已经修复过');
  }
} else {
  console.warn('⚠️  uni-app.es.js 不存在，跳过');
}

console.log('✨ 依赖修复完成！');
