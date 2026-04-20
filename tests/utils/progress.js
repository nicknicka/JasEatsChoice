const fs = require('fs')
const path = require('path')

const PROGRESS_FILE = path.join(__dirname, '..', 'progress.txt')

function updateProgress(id, status, message = '') {
  const content = fs.readFileSync(PROGRESS_FILE, 'utf-8')
  const lines = content.split('\n')
  const updated = lines.map(line => {
    if (line.includes(`] ${id} |`)) {
      return line
        .replace(/\[(PENDING|RUNNING|PASSED|FAILED|SKIPPED)\]/, `[${status}]`)
        .replace(/-$/, message || '-')
    }
    return line
  })
  fs.writeFileSync(PROGRESS_FILE, updated.join('\n'), 'utf-8')
}

function markRunning(id) { updateProgress(id, 'RUNNING', '执行中...') }
function markPassed(id, msg = '通过') { updateProgress(id, 'PASSED', msg) }
function markFailed(id, msg = '失败') { updateProgress(id, 'FAILED', msg) }
function markSkipped(id, msg = '跳过') { updateProgress(id, 'SKIPPED', msg) }

function updateSummary() {
  const content = fs.readFileSync(PROGRESS_FILE, 'utf-8')
  const passed = (content.match(/\[PASSED\]/g) || []).length
  const failed = (content.match(/\[FAILED\]/g) || []).length
  const skipped = (content.match(/\[SKIPPED\]/g) || []).length
  const pending = (content.match(/\[PENDING\]/g) || []).length
  const running = (content.match(/\[RUNNING\]/g) || []).length
  const total = passed + failed + skipped + pending + running
  const rate = total > 0 ? ((passed / total) * 100).toFixed(1) : '0'

  const lines = content.split('\n')
  const updated = lines.map(line => {
    if (line.startsWith('# 总计:')) return `# 总计: ${total} 项记录`
    if (line.startsWith('# PASSED:')) return `# PASSED: ${passed}`
    if (line.startsWith('# FAILED:')) return `# FAILED: ${failed}`
    if (line.startsWith('# SKIPPED:')) return `# SKIPPED: ${skipped}`
    if (line.startsWith('# PENDING:')) return `# PENDING: ${pending}`
    if (line.startsWith('# 通过率:')) return `# 通过率: ${rate}%`
    return line
  })
  fs.writeFileSync(PROGRESS_FILE, updated.join('\n'), 'utf-8')
}

module.exports = { markRunning, markPassed, markFailed, markSkipped, updateSummary }
