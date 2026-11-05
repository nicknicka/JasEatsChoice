#!/usr/bin/env node
/**
 * 由 manifest.json 生成 index.html 索引页与 README.md。
 *
 * 用法：node scripts/build_screenshot_index.js
 * 环境变量：SCREENSHOT_OUT 覆盖产出目录，默认 <repo>/output/web-screenshots
 */
const fs = require('fs')
const path = require('path')

const PROJ = path.resolve(__dirname, '..')
const DIR = process.env.SCREENSHOT_OUT || path.join(PROJ, 'output/web-screenshots')
const manifest = JSON.parse(fs.readFileSync(path.join(DIR, 'manifest.json'), 'utf8'))

const ROLE_META = {
  public: { label: '公共页面', desc: '未登录即可访问：登录 / 注册 / 找回密码 / 商户注册' },
  user: { label: '用户端', desc: '普通用户视角（登录用户 17322222222）' },
  merchant: { label: '商家端', desc: '商家视角（登录用户 17322222222，商家 test）' },
  admin: { label: '管理员端', desc: '管理员视角（admin）' },
}
const ROLE_ORDER = ['public', 'user', 'merchant', 'admin']

const esc = (s) => String(s == null ? '' : s)
  .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')

const redirected = (x) => x.finalUrl && x.url && x.finalUrl !== x.url

function statFor(list) {
  return {
    total: list.length,
    ok: list.filter((x) => x.ok).length,
    redirected: list.filter(redirected).length,
  }
}

const stats = {}
for (const r of ROLE_ORDER) stats[r] = statFor(manifest.filter((x) => x.role === r))
const allStat = statFor(manifest)

// ---------- index.html ----------
const cards = []
for (const role of ROLE_ORDER) {
  const list = manifest.filter((x) => x.role === role)
  if (!list.length) continue
  const meta = ROLE_META[role] || { label: role, desc: '' }
  cards.push(`<section class="group" id="g-${role}">
  <header class="gh">
    <h2>${esc(meta.label)}<span class="count">${list.length}</span></h2>
    <p class="gdesc">${esc(meta.desc)}</p>
  </header>
  <div class="grid">`)
  for (const x of list) {
    const redir = redirected(x)
    cards.push(`    <a class="card${redir ? ' redir' : ''}" href="${esc(x.file)}" target="_blank" rel="noopener">
      <div class="thumb"><img loading="lazy" src="${esc(x.file)}" alt="${esc(x.title)}"></div>
      <div class="meta">
        <span class="ttl">${esc(x.title)}</span>
        <span class="slug">${esc(x.slug)}</span>
        <code class="url">${esc(x.url)}</code>
        ${redir ? `<span class="note">实际跳转到 ${esc(x.finalUrl)}</span>` : ''}
        ${x.note ? `<span class="note">${esc(x.note)}</span>` : ''}
      </div>
    </a>`)
  }
  cards.push('  </div>\n</section>')
}

const nav = ROLE_ORDER
  .filter((r) => stats[r].total)
  .map((r) => `<a href="#g-${r}">${esc((ROLE_META[r] || {}).label || r)} <b>${stats[r].total}</b></a>`)
  .join('\n      ')

const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>JasEatsChoice 全页面截图索引 · ${allStat.total} 张</title>
<style>
  :root{--bg:#f7f4f0;--card:#fff;--ink:#2b2320;--sub:#8a7d74;--line:#e7ded6;--brand:#c07a4a;--warn:#b4581f}
  *{box-sizing:border-box}
  body{margin:0;background:var(--bg);color:var(--ink);font:14px/1.5 -apple-system,"PingFang SC","Microsoft YaHei",system-ui,sans-serif}
  header.top{position:sticky;top:0;z-index:9;background:rgba(247,244,240,.92);backdrop-filter:blur(8px);border-bottom:1px solid var(--line);padding:16px 28px}
  header.top h1{margin:0 0 4px;font-size:18px}
  header.top .sub{color:var(--sub);font-size:12.5px}
  nav{display:flex;gap:8px;flex-wrap:wrap;margin-top:10px}
  nav a{display:inline-flex;align-items:center;gap:6px;padding:5px 12px;border:1px solid var(--line);border-radius:999px;background:#fff;color:var(--ink);text-decoration:none;font-size:13px}
  nav a:hover{border-color:var(--brand);color:var(--brand)}
  nav b{color:var(--brand);font-weight:600}
  main{padding:22px 28px 60px;max-width:1680px;margin:0 auto}
  .group{margin-bottom:40px}
  .gh h2{margin:0;font-size:17px;display:flex;align-items:center;gap:10px}
  .gh .count{font-size:12px;font-weight:500;color:var(--brand);background:#f6e7db;border-radius:999px;padding:1px 9px}
  .gh .gdesc{margin:4px 0 16px;color:var(--sub);font-size:12.5px}
  .grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(320px,1fr));gap:16px}
  .card{display:flex;flex-direction:column;background:var(--card);border:1px solid var(--line);border-radius:12px;overflow:hidden;text-decoration:none;color:inherit;transition:.15s}
  .card:hover{box-shadow:0 8px 24px rgba(80,50,30,.12);transform:translateY(-2px)}
  .card.redir{border-color:#eccfae}
  .thumb{aspect-ratio:16/10;background:#efe8e1;overflow:hidden;display:flex;align-items:center;justify-content:center}
  .thumb img{width:100%;height:100%;object-fit:cover;object-position:top center;display:block}
  .meta{padding:10px 12px;display:flex;flex-direction:column;gap:3px}
  .ttl{font-weight:600;font-size:13.5px}
  .slug{color:var(--sub);font-size:11.5px;letter-spacing:.02em}
  .url{font-size:11.5px;color:#5c5048;background:#f5f1ec;border-radius:6px;padding:2px 6px;overflow-wrap:anywhere}
  .note{font-size:11.5px;color:var(--warn)}
  footer{padding:0 28px 40px;color:var(--sub);font-size:12px;text-align:center}
</style>
</head>
<body>
<header class="top">
  <h1>JasEatsChoice 全页面截图索引</h1>
  <div class="sub">共 ${allStat.total} 张 · 成功 ${allStat.ok} · 视口 1440×900 整页 · 生成于 ${new Date().toISOString().slice(0, 10)}</div>
  <nav>
      ${nav}
  </nav>
</header>
<main>
${cards.join('\n')}
</main>
<footer>点击任意卡片可查看原始尺寸截图（fullPage）。数据由 manifest.json 生成，请勿手改本页。</footer>
</body>
</html>
`
fs.writeFileSync(path.join(DIR, 'index.html'), html)

// ---------- README.md ----------
const lines = []
lines.push('# JasEatsChoice 全页面网页截图')
lines.push('')
lines.push(`共 **${allStat.total}** 张截图（成功 ${allStat.ok}，重定向 ${allStat.redirected}）。视口 1440×900，采用整页（fullPage）截图，桌面端 Web 版。`)
lines.push('')
lines.push(`打开 [index.html](./index.html) 可视化浏览全部截图；原始清单见 [manifest.json](./manifest.json)。`)
lines.push('')
lines.push('## 分组')
lines.push('')
lines.push('| 分组 | 页面数 | 说明 |')
lines.push('|---|---|---|')
for (const r of ROLE_ORDER) {
  if (!stats[r].total) continue
  lines.push(`| ${ROLE_META[r].label} | ${stats[r].total} | ${ROLE_META[r].desc} |`)
}
lines.push('')
lines.push('## 页面清单')
for (const r of ROLE_ORDER) {
  const list = manifest.filter((x) => x.role === r)
  if (!list.length) continue
  lines.push('')
  lines.push(`### ${ROLE_META[r].label}（${list.length}）`)
  lines.push('')
  lines.push('| # | 页面 | 路由 | 截图 | 备注 |')
  lines.push('|---|---|---|---|---|')
  list.forEach((x, i) => {
    const note = redirected(x) ? `实际跳转到 \`${x.finalUrl}\`` : (x.note || '')
    lines.push(`| ${i + 1} | ${x.title} | \`${x.url}\` | [${x.file}](./${x.file}) | ${note} |`)
  })
}
lines.push('')
lines.push('## 复现方式')
lines.push('')
lines.push('前提：后端容器栈已 `make up` 并健康；Web 开发服务器由脚本按需拉起（默认 `http://localhost:5173`）。')
lines.push('')
lines.push('```bash')
lines.push('make screenshots                                            # 起前端 + 全量截图 + 重建索引')
lines.push('ONLY=01-home node scripts/capture_screens.js                # 只补跑指定 slug（按 role/slug 合并进 manifest.json）')
lines.push('node scripts/build_screenshot_index.js                      # 由 manifest.json 重建 index.html 与 README.md')
lines.push('```')
lines.push('')
lines.push('环境变量：`SCREENSHOT_BASE_URL`（Web 基址）、`TEST_API_BASE`（后端 API）、`SCREENSHOT_OUT`（产出目录）。')
lines.push('')
fs.writeFileSync(path.join(DIR, 'README.md'), lines.join('\n'))

console.log(`index.html 与 README.md 已生成：${allStat.total} 张，重定向 ${allStat.redirected} 张`)
