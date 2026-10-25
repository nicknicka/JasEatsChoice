#!/bin/bash
# 清理 UniApp 编译缓存

echo "🧹 清理编译缓存..."

# 清理 unpackage 目录
if [ -d "unpackage" ]; then
  echo "删除 unpackage 目录..."
  rm -rf unpackage
fi

# 清理 node_modules/.cache
if [ -d "node_modules/.cache" ]; then
  echo "删除 node_modules/.cache 目录..."
  rm -rf node_modules/.cache
fi

# 清理 dist 目录
if [ -d "dist" ]; then
  echo "删除 dist 目录..."
  rm -rf dist
fi

echo "✅ 缓存清理完成！"
echo ""
echo "💡 提示：如果问题仍然存在，请重启 HBuilderX 或开发工具"
