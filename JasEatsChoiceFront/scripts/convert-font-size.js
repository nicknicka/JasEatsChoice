#!/usr/bin/env node

/**
 * 字体大小批量转换脚本
 * 将 Vue 组件中的固定 px 字体大小转换为相对 rem 单位
 *
 * 使用方法：
 * node scripts/convert-font-size.js [--dry-run] [--fix]
 *
 * 参数说明：
 * --dry-run  仅显示需要修改的文件，不实际修改（默认）
 * --fix      实际执行修改
 *
 * 示例：
 * node scripts/convert-font-size.js --dry-run  # 先查看哪些文件需要修改
 * node scripts/convert-font-size.js --fix      # 执行批量修改
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// 配置
const CONFIG = {
  srcPath: path.join(__dirname, '../src/renderer/src'),
  fileExtensions: ['.vue'],
  // px 转 rem 的转换比例（基于默认字体大小 14px）
  baseFontSize: 14,
  // 常见字体大小转换表
  conversionTable: {
    '11px': '0.75rem',    // 11/14 ≈ 0.75
    '12px': '0.857rem',   // 12/14 ≈ 0.857
    '13px': '0.929rem',   // 13/14 ≈ 0.929
    '14px': '1rem',       // 14/14 = 1
    '15px': '1.071rem',   // 15/14 ≈ 1.071
    '16px': '1.143rem',   // 16/14 ≈ 1.143
    '18px': '1.286rem',   // 18/14 ≈ 1.286
    '20px': '1.429rem',   // 20/14 ≈ 1.429
    '24px': '1.714rem',   // 24/14 ≈ 1.714
    '28px': '2rem',       // 28/14 = 2
    '32px': '2.286rem',   // 32/14 ≈ 2.286
    '36px': '2.571rem',   // 36/14 ≈ 2.571
    '40px': '2.857rem',   // 40/14 ≈ 2.857
    '48px': '3.429rem',   // 48/14 ≈ 3.429
  }
};

/**
 * 递归获取所有 Vue 文件
 */
function getAllVueFiles(dir, fileList = []) {
  const files = fs.readdirSync(dir);

  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);

    if (stat.isDirectory()) {
      // 跳过 node_modules
      if (file !== 'node_modules' && file !== '.git') {
        getAllVueFiles(filePath, fileList);
      }
    } else if (CONFIG.fileExtensions.includes(path.extname(file))) {
      fileList.push(filePath);
    }
  });

  return fileList;
}

/**
 * 检查文件中是否包含固定字体大小
 */
function checkFile(filePath) {
  try {
    const content = fs.readFileSync(filePath, 'utf8');
    const lines = content.split('\n');
    const matches = [];

    // 匹配 font-size: XXpx 或 font-size="XXpx"
    const regex = /font-size:\s*(\d+)px|font-size="(\d+)px"/gi;

    lines.forEach((line, index) => {
      let match;
      while ((match = regex.exec(line)) !== null) {
        const pxValue = match[1] || match[2];
        const fullMatch = match[0];

        // 跳过已经是 rem 的
        if (fullMatch.includes('rem')) continue;

        matches.push({
          line: index + 1,
          pxValue: pxValue + 'px',
          remValue: CONFIG.conversionTable[pxValue + 'px'] || `${(parseInt(pxValue) / CONFIG.baseFontSize).toFixed(3)}rem`,
          context: line.trim(),
          fullMatch: fullMatch
        });
      }
    });

    return matches;
  } catch (error) {
    console.error(`❌ 读取文件失败: ${filePath}`, error.message);
    return [];
  }
}

/**
 * 转换文件中的字体大小
 */
function convertFile(filePath) {
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    let modified = false;
    let count = 0;

    // 转换 font-size: XXpx（在 style 标签或 style 属性中）
    Object.entries(CONFIG.conversionTable).forEach(([px, rem]) => {
      // 匹配 font-size: XXpx (CSS 样式中)
      const regex1 = new RegExp(`font-size:\\s*${px}\\b`, 'gi');
      const matches1 = content.match(regex1);
      if (matches1) {
        content = content.replace(regex1, `font-size: ${rem} /* 原值: ${px} */`);
        modified = true;
        count += matches1.length;
      }

      // 匹配 font-size="XXpx" (HTML 属性中)
      const regex2 = new RegExp(`font-size=["']${px}["']`, 'gi');
      const matches2 = content.match(regex2);
      if (matches2) {
        content = content.replace(regex2, `font-size="${rem}"`);
        modified = true;
        count += matches2.length;
      }
    });

    if (modified) {
      fs.writeFileSync(filePath, content, 'utf8');
      return { success: true, count };
    }

    return { success: false, count: 0 };
  } catch (error) {
    console.error(`❌ 转换文件失败: ${filePath}`, error.message);
    return { success: false, error: error.message };
  }
}

/**
 * 主函数
 */
function main() {
  const args = process.argv.slice(2);
  const isDryRun = !args.includes('--fix');
  const shouldFix = args.includes('--fix');

  console.log('\n========================================');
  console.log('  字体大小批量转换工具');
  console.log('========================================\n');
  console.log(`📁 扫描目录: ${CONFIG.srcPath}`);
  console.log(`🔍 模式: ${isDryRun ? '预览模式（不修改文件）' : '修改模式'}`);
  console.log(`📏 基准字体: ${CONFIG.baseFontSize}px\n`);

  // 获取所有 Vue 文件
  console.log('⏳ 正在扫描文件...\n');
  const allFiles = getAllVueFiles(CONFIG.srcPath);
  console.log(`📊 共找到 ${allFiles.length} 个 Vue 文件\n`);

  // 检查每个文件
  const results = [];
  let totalMatches = 0;

  allFiles.forEach(filePath => {
    const matches = checkFile(filePath);
    if (matches.length > 0) {
      const relativePath = path.relative(CONFIG.srcPath, filePath);
      results.push({
        file: relativePath,
        fullPath: filePath,
        matches: matches
      });
      totalMatches += matches.length;
    }
  });

  // 显示结果
  if (results.length === 0) {
    console.log('✅ 太棒了！没有发现需要修改的固定字体大小。\n');
    return;
  }

  console.log(`⚠️  发现 ${results.length} 个文件需要修改，共 ${totalMatches} 处\n`);

  // 显示详细信息
  results.forEach(result => {
    console.log(`📄 ${result.file}`);
    result.matches.forEach(match => {
      const indent = '   '.repeat(Math.ceil(match.pxValue.length / 4));
      console.log(`${indent}└─ 行 ${match.line}: ${match.pxValue} → ${match.remValue}`);
      console.log(`      ${match.context.substring(0, 60)}...`);
    });
    console.log('');
  });

  // 如果不是预览模式，执行修改
  if (shouldFix) {
    console.log('\n🔧 开始批量修改...\n');
    let successCount = 0;
    let totalConverted = 0;

    results.forEach(result => {
      const conversion = convertFile(result.fullPath);
      if (conversion.success) {
        successCount++;
        totalConverted += conversion.count;
        console.log(`✅ ${result.file}: 已修改 ${conversion.count} 处`);
      } else if (conversion.error) {
        console.log(`❌ ${result.file}: 修改失败 - ${conversion.error}`);
      }
    });

    console.log('\n========================================');
    console.log(`✅ 完成！成功修改 ${successCount} 个文件，共 ${totalConverted} 处`);
    console.log('========================================\n');

    // 提示重新启动开发服务器
    console.log('💡 提示：请重新启动开发服务器以查看效果\n');
  } else {
    console.log('========================================');
    console.log('💡 这是预览模式，没有实际修改文件');
    console.log('   如需执行修改，请运行：');
    console.log(`   node ${path.relative(process.cwd(), __filename)} --fix`);
    console.log('========================================\n');
  }
}

// 运行主函数
main();
