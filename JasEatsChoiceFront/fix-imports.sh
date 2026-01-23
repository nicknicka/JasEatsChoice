#!/bin/bash
# 修复AI组件导入路径问题

echo "开始修复导入路径..."

# 1. 修复composables中的导入
echo "修复composables..."
find src/renderer/src/composables -name "*.js" -type f -exec sed -i '' \
  -e "s|from '../config/index\.js'|from '../config/index'|g" \
  -e "s|from '../config/chatConfig\.js'|from '../config/chatConfig'|g" \
  -e "s|from '../store/[^']*\.js'|from '../store/&|g; s|from '../store/\(.*\)\.js'|from '../store/\1'|g" \
  -e "s|from '../utils/[^']*\.js'|from '../utils/&|g; s|from '../utils/\(.*\)\.js'|from '../utils/\1'|g" \
  -e "s|from './\([^']*\.js\)'|from './\1'|g; s|from './\([^']*\)\.js'|from './\1'|g" \
  {} \;

# 2. 修复utils中的导入
echo "修复utils..."
find src/renderer/src/utils -name "*.js" -type f -exec sed -i '' \
  -e "s|from '../config/chatConfig\.js'|from '../config/chatConfig'|g" \
  -e "s|from '../utils/[^']*\.js'|from '../utils/&|g; s|from '../utils/\(.*\)\.js'|from '../utils/\1'|g" \
  {} \;

# 3. 修复组件中的导入
echo "修复Vue组件..."
find src/renderer/src/views/user/AI/components -name "*.vue" -type f -exec sed -i '' \
  -e "s|from '../../../config/chatConfig\.js'|from '../../../config/chatConfig'|g" \
  -e "s|from '../../../config/index\.js'|from '../../../config/index'|g" \
  -e "s|from '../../../composables/[^']*\.js'|from '../../../composables/&|g; s|from '../../../composables/\(.*\)\.js'|from '../../../composables/\1'|g" \
  -e "s|from '../../../utils/[^']*\.js'|from '../../../utils/&|g; s|from '../../../utils/\(.*\)\.js'|from '../../../utils/\1'|g" \
  -e "s|from '../../../components/common/[^']*\.vue'|from '../../../components/common/&|g; s|from '../../../components/common/\(.*\)\.vue'|from '../../../components/common/\1'|g" \
  {} \;

echo "✅ 修复完成！"
echo ""
echo "请按以下步骤测试："
echo "1. 停止当前开发服务器 (Ctrl+C)"
echo "2. 清除缓存: rm -rf node_modules/.vite"
echo "3. 重新启动: npm run dev"
