#!/bin/bash

# 推荐拒绝功能测试脚本
# 用于验证后端API是否正常工作

echo "================================"
echo "推荐拒绝功能测试脚本"
echo "================================"
echo ""

BASE_URL="http://localhost:8080/api"
USER_ID="1"
DISH_ID="1001"

echo "📋 测试准备..."
echo "BASE_URL: $BASE_URL"
echo "USER_ID: $USER_ID"
echo "DISH_ID: $DISH_ID"
echo ""

# 测试1: 记录拒绝
echo "🧪 测试1: 记录拒绝推荐"
curl -X POST "${BASE_URL}/v1/recommend/reject?userId=${USER_ID}&dishId=${DISH_ID}&reason=测试拒绝" \
  -H "Content-Type: application/json" \
  -w "\n状态码: %{http_code}\n\n"

sleep 1

# 测试2: 统计拒绝次数
echo "🧪 测试2: 统计拒绝次数"
curl -X GET "${BASE_URL}/v1/recommend/reject/count?userId=${USER_ID}&dishId=${DISH_ID}" \
  -H "Content-Type: application/json" \
  -w "\n状态码: %{http_code}\n\n"

sleep 1

# 测试3: 再次拒绝同一菜品
echo "🧪 测试3: 再次拒绝同一菜品（测试次数累加）"
curl -X POST "${BASE_URL}/v1/recommend/reject?userId=${USER_ID}&dishId=${DISH_ID}&reason=再次拒绝" \
  -H "Content-Type: application/json" \
  -w "\n状态码: %{http_code}\n\n"

sleep 1

# 测试4: 获取已拒绝列表
echo "🧪 测试4: 获取已拒绝菜品列表"
curl -X GET "${BASE_URL}/v1/recommend/reject/list?userId=${USER_ID}" \
  -H "Content-Type: application/json" \
  -w "\n状态码: %{http_code}\n\n"

sleep 1

# 测试5: 获取频繁拒绝的菜品（阈值2）
echo "🧪 测试5: 获取频繁拒绝的菜品（阈值2）"
curl -X GET "${BASE_URL}/v1/recommend/reject/frequent?userId=${USER_ID}&threshold=2" \
  -H "Content-Type: application/json" \
  -w "\n状态码: %{http_code}\n\n"

sleep 1

# 测试6: 清除拒绝记录
echo "🧪 测试6: 清除拒绝记录"
curl -X DELETE "${BASE_URL}/v1/recommend/reject?userId=${USER_ID}&dishId=${DISH_ID}" \
  -H "Content-Type: application/json" \
  -w "\n状态码: %{http_code}\n\n"

echo "================================"
echo "✅ 测试完成"
echo "================================"
echo ""
echo "💡 提示："
echo "1. 所有测试应该返回 200 状态码"
echo "2. 检查返回的 JSON 数据格式是否正确"
echo "3. 查看后端日志验证SQL执行情况"
echo ""
