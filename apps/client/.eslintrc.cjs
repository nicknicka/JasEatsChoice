/* eslint-env node */
require('@rushstack/eslint-patch/modern-module-resolution')

module.exports = {
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended',
    '@electron-toolkit',
    '@vue/eslint-config-prettier'
  ],
  globals: {
    // 高德地图 JS API 由 index.html 的 <script> 注入，浏览器运行时才存在。
    AMap: 'readonly',
    // useAmapLocation.js 用 globalThis 挂载探测，属 ES2021 标准全局。
    globalThis: 'readonly'
  },
  rules: {
    'vue/require-default-prop': 'off',
    'vue/multi-word-component-names': 'off'
  }
}
