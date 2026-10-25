const fs = require('fs')
const path = require('path')

const PROGRESS_FILE = path.join(__dirname, '..', 'progress.txt')

const LEGEND = [
  '# 佳食宜选自动化测试进度记录',
  '# 格式：[状态] 阶段-编号 | 功能名称 | 结果说明',
  '# 状态：PENDING(待执行) / RUNNING(执行中) / PASSED(通过) / FAILED(失败) / SKIPPED(跳过)'
]

// progress.txt 只是运行台账，缺失或不含某个编号时不能让整条用例因为写进度而失败。
function readLines() {
  if (!fs.existsSync(PROGRESS_FILE)) {
    return [...LEGEND, '']
  }
  return fs.readFileSync(PROGRESS_FILE, 'utf-8').split('\n')
}

function updateProgress(id, status, message = '') {
  const lines = readLines()
  const index = lines.findIndex(line => line.includes(`] ${id} |`))
  if (index === -1) {
    lines.push(`[${status}] ${id} | - | ${message}`)
  } else {
    lines[index] = lines[index]
      .replace(/\[(PENDING|RUNNING|PASSED|FAILED|SKIPPED)\]/, `[${status}]`)
      .replace(/-$/, message || '-')
  }
  fs.writeFileSync(PROGRESS_FILE, lines.join('\n'), 'utf-8')
}

function markRunning(id) { updateProgress(id, 'RUNNING', '执行中...') }
function markPassed(id, msg = '通过') { updateProgress(id, 'PASSED', msg) }
function markFailed(id, msg = '失败') { updateProgress(id, 'FAILED', msg) }
function markSkipped(id, msg = '跳过') { updateProgress(id, 'SKIPPED', msg) }

function updateSummary() {
  const lines = readLines()
  const body = lines.filter(line => /^\[(PENDING|RUNNING|PASSED|FAILED|SKIPPED)\] /.test(line))
  const count = status => body.filter(line => line.startsWith(`[${status}]`)).length
  const passed = count('PASSED')
  const failed = count('FAILED')
  const skipped = count('SKIPPED')
  const pending = count('PENDING')
  const running = count('RUNNING')
  const total = passed + failed + skipped + pending + running
  const rate = total > 0 ? ((passed / total) * 100).toFixed(1) : '0'

  const summary = [
    `# 总计: ${total} 项记录`,
    `# PASSED: ${passed}`,
    `# FAILED: ${failed}`,
    `# SKIPPED: ${skipped}`,
    `# PENDING: ${pending}`,
    `# 通过率: ${rate}%`
  ]

  // 已有汇总行就地更新，缺失的补在图例后面，保证文件里只有一份汇总。
  const remaining = lines.filter(line => !/^# (总计:|PASSED:|FAILED:|SKIPPED:|PENDING:|通过率:)/.test(line))
  const legendIndex = remaining.findIndex(line => line.startsWith('# 状态：'))
  if (legendIndex === -1) {
    remaining.unshift(...summary)
  } else {
    remaining.splice(legendIndex + 1, 0, ...summary)
  }
  fs.writeFileSync(PROGRESS_FILE, remaining.join('\n'), 'utf-8')
}

module.exports = { markRunning, markPassed, markFailed, markSkipped, updateSummary }
