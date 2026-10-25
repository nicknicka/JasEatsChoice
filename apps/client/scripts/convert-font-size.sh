#!/bin/bash

# ==========================================
# 字体大小批量转换脚本（Bash 版本）
# ==========================================
# 将 Vue 组件中的固定 px 字体大小转换为相对 rem 单位
#
# 使用方法：
#   ./scripts/convert-font-size.sh          # 预览模式（不修改文件）
#   ./scripts/convert-font-size.sh --fix    # 执行修改
#
# 示例：
#   ./scripts/convert-font-size.sh --dry-run   # 查看哪些文件需要修改
#   ./scripts/convert-font-size.sh --fix       # 执行批量修改
# ==========================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
SRC_DIR="./src/renderer/src"
FIX_MODE=false

# 检查参数
if [[ "$1" == "--fix" ]]; then
    FIX_MODE=true
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  字体大小批量转换工具${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "📁 扫描目录: ${SRC_DIR}"
if [ "$FIX_MODE" = true ]; then
    echo -e "🔧 模式: ${YELLOW}修改模式（会修改文件）${NC}"
else
    echo -e "🔍 模式: ${GREEN}预览模式（不修改文件）${NC}"
fi
echo ""

# 检查目录是否存在
if [ ! -d "$SRC_DIR" ]; then
    echo -e "${RED}❌ 错误：目录不存在 $SRC_DIR${NC}"
    exit 1
fi

# 查找所有包含 font-size: XXpx 的 Vue 文件
echo -e "${BLUE}⏳ 正在扫描文件...${NC}"
echo ""

# 使用 grep 查找
FILES=$(grep -rl "font-size:.*[0-9]\+px" "$SRC_DIR" --include="*.vue" 2>/dev/null)

if [ -z "$FILES" ]; then
    echo -e "${GREEN}✅ 太棒了！没有发现需要修改的固定字体大小。${NC}"
    echo ""
    exit 0
fi

# 统计
FILE_COUNT=0
TOTAL_MATCHES=0

# 显示每个文件的匹配情况
while IFS= read -r file; do
    FILE_COUNT=$((FILE_COUNT + 1))
    REL_PATH="${file#$SRC_DIR/}"

    echo -e "${YELLOW}📄 $REL_PATH${NC}"

    # 显示每个匹配的行
    grep -n "font-size:.*[0-9]\+px" "$file" | while IFS=: read -r line_num content; do
        # 提取 px 值并转换
        if [[ $content =~ font-size:\s*([0-9]+)px ]]; then
            px_value="${BASH_REMATCH[1]}"
            rem_value=$(awk "BEGIN {printf \"%.3f\", $px_value / 14}")

            case $px_value in
                11) rem_value="0.75rem" ;;
                12) rem_value="0.857rem" ;;
                13) rem_value="0.929rem" ;;
                14) rem_value="1rem" ;;
                15) rem_value="1.071rem" ;;
                16) rem_value="1.143rem" ;;
                18) rem_value="1.286rem" ;;
                20) rem_value="1.429rem" ;;
                24) rem_value="1.714rem" ;;
                28) rem_value="2rem" ;;
                32) rem_value="2.286rem" ;;
            esac

            echo -e "   ${NC}行 $line_num: ${px_value}px → ${GREEN}$rem_value${NC}"
            TOTAL_MATCHES=$((TOTAL_MATCHES + 1))
        fi
    done
    echo ""
done <<< "$FILES"

echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}⚠️  发现 $FILE_COUNT 个文件需要修改，共 $TOTAL_MATCHES 处${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 如果是修改模式，执行修改
if [ "$FIX_MODE" = true ]; then
    echo -e "${BLUE}🔧 开始批量修改...${NC}"
    echo ""
    echo -e "${RED}⚠️  警告：此操作将修改文件，建议先提交代码到 Git！${NC}"
    echo ""
    read -p "确定要继续吗？(y/N) " -n 1 -r
    echo ""

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        SUCCESS_COUNT=0
        CONVERTED_COUNT=0

        while IFS= read -r file; do
            # 备份文件
            cp "$file" "${file}.bak"

            # 执行替换
            # 11px
            sed -i '' 's/font-size: 11px/font-size: 0.75rem \/* 原值: 11px *\//g' "$file" 2>/dev/null || sed -i 's/font-size: 11px/font-size: 0.75rem \/* 原值: 11px *\//g' "$file"
            # 12px
            sed -i '' 's/font-size: 12px/font-size: 0.857rem \/* 原值: 12px *\//g' "$file" 2>/dev/null || sed -i 's/font-size: 12px/font-size: 0.857rem \/* 原值: 12px *\//g' "$file"
            # 13px
            sed -i '' 's/font-size: 13px/font-size: 0.929rem \/* 原值: 13px *\//g' "$file" 2>/dev/null || sed -i 's/font-size: 13px/font-size: 0.929rem \/* 原值: 13px *\//g' "$file"
            # 14px
            sed -i '' 's/font-size: 14px/font-size: 1rem \/* 原值: 14px *\//g' "$file" 2>/dev/null || sed -i 's/font-size: 14px/font-size: 1rem \/* 原值: 14px *\//g' "$file"
            # 16px
            sed -i '' 's/font-size: 16px/font-size: 1.143rem \/* 原值: 16px *\//g' "$file" 2>/dev/null || sed -i 's/font-size: 16px/font-size: 1.143rem \/* 原值: 16px *\//g' "$file"
            # 18px
            sed -i '' 's/font-size: 18px/font-size: 1.286rem \/* 原值: 18px *\//g' "$file" 2>/dev/null || sed -i 's/font-size: 18px/font-size: 1.286rem \/* 原值: 18px *\//g' "$file"
            # 20px
            sed -i '' 's/font-size: 20px/font-size: 1.429rem \/* 原值: 20px *\//g' "$file" 2>/dev/null || sed -i 's/font-size: 20px/font-size: 1.429rem \/* 原值: 20px *\//g' "$file"
            # 24px
            sed -i '' 's/font-size: 24px/font-size: 1.714rem \/* 原值: 24px *\//g' "$file" 2>/dev/null || sed -i 's/font-size: 24px/font-size: 1.714rem \/* 原值: 24px *\//g' "$file"

            # 检查是否修改成功
            if [ $? -eq 0 ]; then
                REL_PATH="${file#$SRC_DIR/}"
                MATCHES=$(grep -c "font-size:.*rem \/\* 原值:" "$file" 2>/dev/null || echo 0)
                if [ $MATCHES -gt 0 ]; then
                    echo -e "${GREEN}✅ $REL_PATH: 已修改${NC}"
                    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
                    CONVERTED_COUNT=$((CONVERTED_COUNT + MATCHES))

                    # 删除备份
                    rm "${file}.bak"
                else
                    # 恢复备份
                    mv "${file}.bak" "$file"
                fi
            else
                # 恢复备份
                mv "${file}.bak" "$file"
            fi
        done <<< "$FILES"

        echo ""
        echo -e "${BLUE}========================================${NC}"
        echo -e "${GREEN}✅ 完成！成功修改 $SUCCESS_COUNT 个文件，共 $CONVERTED_COUNT 处${NC}"
        echo -e "${BLUE}========================================${NC}"
        echo ""
        echo -e "${YELLOW}💡 提示：请重新启动开发服务器以查看效果${NC}"
        echo ""
    else
        echo -e "${YELLOW}❌ 已取消操作${NC}"
        echo ""
    fi
else
    echo -e "${BLUE}========================================${NC}"
    echo -e "${YELLOW}💡 这是预览模式，没有实际修改文件${NC}"
    echo -e "${YELLOW}   如需执行修改，请运行：${NC}"
    echo -e "${GREEN}   ./scripts/convert-font-size.sh --fix${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
fi
