# #583 / #584 H5 构建链修复记录

## 背景

小程序端（`apps/miniapp`）声明了 `dev:h5` / `build:h5` 两个脚本，但这条路径
长期**静默失效**：构建退出码 0、无警告，产物却只有约 12 KB。本记录整理两个
独立缺陷的根因、修法与验证，尤其是 #584 的方案取舍依据。

两件事的因果关系：

- **#583**：H5 平台插件**没注册** → H5 根本跑不起来；
- **#584**：`#583` 修好后 H5 能跑了，但**内嵌 Vue 运行时**与项目锁定的 Vue
  版本错配 → 带 `v-if`/`v-else-if` 兄弟链的块静默不渲染。

`#584` 的复现依赖 `#583` 先修好。

---

## #583：`package.json` 缺 H5 平台依赖声明

### 根因

uni 的平台插件**不是靠目录探测，而是按项目 `package.json` 的依赖列表去 require**
（`node_modules/@dcloudio/vite-plugin-uni/dist/utils/plugin.js` 的
`resolvePluginsByCliRoot`）：

```js
function resolvePluginsByCliRoot(cliRoot, platform, uvue = false) {
    const pkg = require(path.join(cliRoot, 'package.json'));
    return resolvePlugins(Object.keys(pkg.devDependencies || {})
        .concat(Object.keys(pkg.dependencies || {}))
        .map((id) => { ... }), platform, uvue);
}
```

`@dcloudio/uni-h5` 与 `@dcloudio/uni-h5-vite` 当时只是 `@dcloudio/uni-components`
的**传递依赖**，不在项目依赖列表里 → H5 平台插件不注册 → `src/main.js` 的
`createApp()` 从未被调用。

### 失败形态（复现基线）

```
$ npm run build:h5
DONE  Build complete.          ← 退出码 0，无警告
$ ls -l dist/build/h5/assets/
index-CSmwmQ2l.js    12,465 bytes   ← 只有 pinia，没有应用入口
index-U6bz3gL0.css    7,221 bytes
```

dev 模式同样：`#app` 为空、`window.__uniConfig` 为 `undefined`、控制台无报错。

### 修法

在 `apps/miniapp/package.json` 的 `devDependencies` 声明两个平台包，
版本与 `@dcloudio/uni-app` 对齐（`^3.0.0-alpha-5000720260327001`）：

```json
"@dcloudio/uni-h5": "^3.0.0-alpha-5000720260327001",
"@dcloudio/uni-h5-vite": "^3.0.0-alpha-5000720260327001",
```

### 防回归门禁

- 脚本：`scripts/check_h5_build.py`
- 单测：`scripts/test_check_h5_build.py`（7 条，随 `make check-unit` 跑）
- Makefile：`make build-h5`（构建 + 校验）、`make check-h5`（只校验既有产物）
- CI：`.github/workflows/smoke.yml` 的 `miniapp` job 新增 `make build-h5`

判据（产物本身，而不是构建退出码）：

| 判据 | 阈值 | 理由 |
|---|---|---|
| `index.html` 存在 | — | 缺了就不是可部署产物 |
| 产物总字节 | ≥ 500 KiB | 空壳约 12–19 KB，完整约 2 MB，500 KiB 留足余量 |
| 页面级 chunk 数（`assets/pages-*.js`） | ≥ 1 | 比体积更本质：空壳的典型特征就是**零页面 chunk** |

---

## #584：`uni-h5-vue` 内嵌 Vue 3.4.21 与项目锁定 3.5.13 错配

### 根因

`@dcloudio/uni-h5-vue` **内嵌 Vue 3.4.21 运行时**（不是 peerDependency）：

```
$ head -3 node_modules/@dcloudio/uni-h5-vue/dist/vue.runtime.esm.js
/**
* @dcloudio/uni-h5-vue v3.4.21
```

而项目 `overrides` 把 Vue 全家桶锁在 **3.5.13**。3.4 → 3.5 之间 slot 归一化实现有变：

- **3.4.21** `updateSlots` 走 `extend(slots, children)`，直接把 `children` 的所有键
  写入 `slots`（含 `_`）；
- **3.5.13** 的编译器产出的 children 对象把 `_` 定义为**只读**
  （`def(children, "_", type)` 且 `writable` 默认 `false`）→ 赋值抛
  `TypeError: Cannot assign to read only property '_'`，整棵子树不渲染。

该错配只在 H5 暴露：mp-weixin 走 `uni-mp-vue` 自己的运行时路径，不触发这段更新逻辑。

### 方案评估（这是本次最重要的决策）

issue 给了三个候选修法，逐个评估：

#### 方案 2（升级 `@dcloudio/*` 到内嵌 Vue ≥ 3.5 的版本）——**已证伪**

抽查 npm 上的版本，**内嵌 Vue 恒为 3.4.21**，横跨 2026-05 到 2026-11 全部无例外：

```
3.0.0-alpha-5010120260519001 -> @vue/server-renderer=3.4.21
3.0.0-alpha-5020120260706001 -> @vue/server-renderer=3.4.21
3.0.0-alpha-5030120260930001 -> @vue/server-renderer=3.4.21
3.0.0-alpha-5030620261112001 -> @vue/server-renderer=3.4.21   ← 抽查时的最新版
```

即上游 dcloudio 当前根本没有内嵌 Vue ≥ 3.5 的版本可升，方案 2 无路可走。

#### 方案 1（Vue 全家桶降到 3.4.21，与内嵌运行时对齐）——**采纳**

**关键洞察**：`uni-mp-vue`（mp-weixin 用的运行时）内嵌的**也是 Vue 3.4.21**：

```
$ cat node_modules/@dcloudio/uni-mp-vue/package.json
"dependencies": { "@vue/shared": "3.4.21", "@dcloudio/uni-shared": "..." }
```

也就是说：

- **交付面（mp-weixin）的生产运行时本来就是 3.4.21**；
- `overrides` 锁住的 3.5.13 只锁住了**编译器与构建工具链**（`@vue/compiler-sfc`
  等），让它与内嵌运行时不一致。

因此降级不是「引入一个更旧的版本」，而是让**编译器与内嵌运行时对齐**——
这正是消除错配的正确方向。

**实测验证**（隔离目录，避免污染主仓依赖）：

| 验证项 | 结果 |
|---|---|
| `npm run build:h5` | ✅ 2.6 MB / 220 文件 / 85 页面 chunk |
| `review-submit` 页 H5 渲染 | ✅ 出现「评价对象加载失败 + 重试」，**无 `updateSlots` 报错** |
| `npm run build:mp-weixin` | ✅ 构建通过 |
| 关键页 `index.wxml` | ✅ `wx:elif="{{f}}" ... target-error` 分支**字节级一致** |
| mp-weixin 产物差异 | ✅ 仅 5 个文件差异（vendor.js 版本号 banner 等），关键页 wxml 完全相同 |

#### 方案 3（明确声明 H5 不受支持）——**未采纳**

调研确认 H5 在本项目里的定位（依据）：

- CI（`.github/workflows/` 三个 workflow）**无任何 H5 引用**；
- `Makefile` 此前**无 H5 target**；
- `apps/miniapp/docs/小程序端首轮浏览器核验问题清单.md` 写明：
  「`H5` 仅作为辅助截图和宿主排障面，不作为本轮通过标准」；
- 但 `docs/evidence/issue-561/README.md` 记录了 H5 是**真实的取证/跨平台验证通道**
  （本机无微信开发者工具，断网态截图只能靠 H5 产出）。

结论：H5 虽不进 CI、也非交付面，但**是唯一的跨平台验证手段**，直接放弃会损失
「用 H5 验证页面逻辑」的能力（#561 的取证就依赖它）。方案 1 既修好错配、
又保住这条通道，且回归代价很小（见上表），因此优于方案 3。

### 修法落地

`apps/miniapp/package.json`：`vue`、`@vue/compiler-sfc` 与 `overrides` 全部
由 `3.5.13` → `3.4.21`，与 `@dcloudio/uni-h5-vue` / `@dcloudio/uni-mp-vue`
内嵌运行时对齐。

### 顺带发现：`fix-dependencies.js` 补丁的现状

`apps/miniapp/scripts/fix-dependencies.js`（postinstall）原本是为了给
`@vue/runtime-core` 补 `injectHook` / `isInSSRComponentSetup` 两个导出
（3.5.13 不导出，而 dcloudio alpha 需要）。降到 3.4.21 后：

- `@vue/runtime-core` 的布局变了（嵌在 `@vue/runtime-dom/node_modules/` 下），
  脚本第一步打印「⚠️ runtime-core.esm-bundler.js 不存在，跳过」——**补丁已自然失效**；
- 实测**不跑这一步也能构建成功**（3.4.21 的导出面与 dcloudio 期望一致）。

本 PR **未改动** `fix-dependencies.js`：它现在是无害的空转（幂等、找不到就跳过），
留着不影响任何构建结果。若要清理，应作为独立改动评估，不在本 issue 范围内。

---

## 验证命令与结果

```bash
# H5 侧
cd apps/miniapp && npm run build:h5      # 2.6 MB / 251 文件 / 85 页面级 chunk
python3 scripts/check_h5_build.py        # 产物完整

# H5 页面渲染（静态服务 + Playwright）
python3 -m http.server 8931 --directory dist/build/h5
# 打开 http://127.0.0.1:8931/#/pages-user/review/submit/index?type=dish&id=1
#   ✅ bodyText 含「评价对象加载失败」
#   ✅ 无 updateSlots TypeError
#   ✅ #app 有渲染内容、window.__uniConfig 为 object

# mp-weixin 回归
make build-miniapp                       # ✅
# pages-user/review/submit/index.wxml 保留 wx:elif 错误态分支

# 静态防线
make check                               # ✅ 104 条单测全绿
```

---

## 未决 / 不确定处

- `make check` 本身**不包含** H5 门禁：`check` 定位是「不起容器、不构建」的静态
  防线，而 H5 门禁必须先有产物。故挂在 CI 的 `miniapp` job（已装 miniapp 依赖）
  与独立 target `make build-h5` / `make check-h5`。
- 降级 Vue 到 3.4.21 后，桌面端（`apps/client`，独立 Vue 3.5.32）与小程序
  `src/` 未使用任何 3.5 专有 API（已 grep `useTemplateRef` / `useId` /
  `onWatcherCleanup` / `defineModel`，命中 0），但**未做真机微信开发者工具的
  运行时回归**（本机无该工具）——产物级对比是关键页 wxml 字节级一致。
