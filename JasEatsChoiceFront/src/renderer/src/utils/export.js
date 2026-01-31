/**
 * 数据导出工具
 * 使用xlsx库实现Excel导出
 */
import * as XLSX from 'xlsx'

/**
 * 导出数据到Excel
 * @param {Array} data - 要导出的数据数组
 * @param {Object} options - 配置选项
 * @param {string} options.filename - 文件名（不含扩展名）
 * @param {Array} options.headers - 表头配置 [{key: '字段名', label: '列名', width: 20}]
 * @param {string} options.sheetName - 工作表名称
 */
export function exportToExcel(data, options = {}) {
  const {
    filename = '导出数据',
    headers = [],
    sheetName = 'Sheet1'
  } = options

  try {
    // 如果没有提供表头配置，自动从第一条数据提取
    let finalHeaders = headers
    if (!headers || headers.length === 0) {
      if (data.length > 0) {
        finalHeaders = Object.keys(data[0]).map(key => ({
          key,
          label: key,
          width: 15
        }))
      } else {
        console.error('没有数据可以导出')
        return
      }
    }

    // 转换数据为二维数组
    const wsData = []

    // 添加表头
    wsData.push(finalHeaders.map(h => h.label))

    // 添加数据行
    data.forEach(row => {
      const rowData = finalHeaders.map(header => {
        const value = row[header.key]
        // 格式化数据
        if (value === null || value === undefined) {
          return ''
        }
        if (typeof value === 'object') {
          return JSON.stringify(value)
        }
        return value
      })
      wsData.push(rowData)
    })

    // 创建工作表
    const ws = XLSX.utils.aoa_to_sheet(wsData)

    // 设置列宽
    const colWidths = finalHeaders.map(h => ({ wch: h.width || 15 }))
    ws['!cols'] = colWidths

    // 创建工作簿
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, sheetName)

    // 导出文件
    const fileName = `${filename}_${new Date().getTime()}.xlsx`
    XLSX.writeFile(wb, fileName)

    return true
  } catch (error) {
    console.error('导出Excel失败:', error)
    return false
  }
}

/**
 * 导出CSV文件
 * @param {Array} data - 要导出的数据数组
 * @param {Object} options - 配置选项
 * @param {string} options.filename - 文件名（不含扩展名）
 * @param {Array} options.headers - 表头配置
 */
export function exportToCSV(data, options = {}) {
  const {
    filename = '导出数据',
    headers = []
  } = options

  try {
    // 如果没有提供表头配置，自动从第一条数据提取
    let finalHeaders = headers
    if (!headers || headers.length === 0) {
      if (data.length > 0) {
        finalHeaders = Object.keys(data[0]).map(key => ({
          key,
          label: key
        }))
      } else {
        console.error('没有数据可以导出')
        return
      }
    }

    // 构建CSV内容
    let csvContent = ''

    // 添加表头
    csvContent += finalHeaders.map(h => h.label).join(',') + '\n'

    // 添加数据行
    data.forEach(row => {
      const rowData = finalHeaders.map(header => {
        const value = row[header.key]
        if (value === null || value === undefined) {
          return ''
        }
        // 转义包含逗号或引号的字段
        const stringValue = String(value)
        if (stringValue.includes(',') || stringValue.includes('"') || stringValue.includes('\n')) {
          return `"${stringValue.replace(/"/g, '""')}"`
        }
        return stringValue
      })
      csvContent += rowData.join(',') + '\n'
    })

    // 添加BOM以支持中文
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })

    // 创建下载链接
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `${filename}_${new Date().getTime()}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    return true
  } catch (error) {
    console.error('导出CSV失败:', error)
    return false
  }
}

/**
 * 导出JSON文件
 * @param {Array} data - 要导出的数据数组
 * @param {string} filename - 文件名（不含扩展名）
 */
export function exportToJSON(data, filename = '导出数据') {
  try {
    const jsonContent = JSON.stringify(data, null, 2)
    const blob = new Blob([jsonContent], { type: 'application/json;charset=utf-8;' })

    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `${filename}_${new Date().getTime()}.json`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    return true
  } catch (error) {
    console.error('导出JSON失败:', error)
    return false
  }
}

/**
 * 打印数据
 * @param {Array} data - 要打印的数据数组
 * @param {Object} options - 配置选项
 * @param {string} options.title - 标题
 * @param {Array} options.headers - 表头配置
 */
export function printData(data, options = {}) {
  const {
    title = '打印数据',
    headers = []
  } = options

  // 如果没有提供表头配置，自动从第一条数据提取
  let finalHeaders = headers
  if (!headers || headers.length === 0) {
    if (data.length > 0) {
      finalHeaders = Object.keys(data[0]).map(key => ({
        key,
        label: key
      }))
    }
  }

  // 构建HTML表格
  let tableHTML = `
    <html>
    <head>
      <title>${title}</title>
      <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; font-weight: bold; }
        h1 { text-align: center; }
      </style>
    </head>
    <body>
      <h1>${title}</h1>
      <table>
        <thead>
          <tr>
            ${finalHeaders.map(h => `<th>${h.label}</th>`).join('')}
          </tr>
        </thead>
        <tbody>
          ${data.map(row => `
            <tr>
              ${finalHeaders.map(h => `<td>${row[h.key] || ''}</td>`).join('')}
            </tr>
          `).join('')}
        </tbody>
      </table>
    </body>
    </html>
  `

  // 打印
  const printWindow = window.open('', '_blank')
  printWindow.document.write(tableHTML)
  printWindow.document.close()
  printWindow.print()
}
