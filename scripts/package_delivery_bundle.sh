#!/bin/zsh

# Legacy packager: paths match the archived repo layout (JasEatsChoiceJava/ JasEatsChoiceFront/ docker/ database/), not apps/ here.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
DATE_TAG="$(date +%Y%m%d)"
PACKAGE_NAME="JasEatsChoice_Delivery_Package_${DATE_TAG}"
OUTPUT_DIR="$ROOT_DIR/output/delivery-package"
STAGE_DIR="$OUTPUT_DIR/$PACKAGE_NAME"
ZIP_PATH="$OUTPUT_DIR/${PACKAGE_NAME}.zip"

mkdir -p "$OUTPUT_DIR"
rm -rf "$STAGE_DIR" "$ZIP_PATH"

mkdir -p \
  "$STAGE_DIR/00_delivery_docs" \
  "$STAGE_DIR/01_source_code" \
  "$STAGE_DIR/02_project_docs" \
  "$STAGE_DIR/03_database_scripts/migrations" \
  "$STAGE_DIR/03_database_scripts/java-sql" \
  "$STAGE_DIR/03_database_scripts/nutrition"

copy_tree() {
  local source_dir="$1"
  local target_dir="$2"

  if [[ ! -d "$source_dir" ]]; then
    return 0
  fi

  rsync -a \
    --exclude '.git/' \
    --exclude '.github/' \
    --exclude '.idea/' \
    --exclude '.vscode/' \
    --exclude '.claude/' \
    --exclude '.omx/' \
    --exclude '.obsidian/' \
    --exclude '.artifacts/' \
    --exclude 'node_modules/' \
    --exclude 'target/' \
    --exclude 'dist/' \
    --exclude 'out/' \
    --exclude 'build/' \
    --exclude 'unpackage/' \
    --exclude 'uploads/' \
    --exclude 'output/' \
    --exclude 'reports/' \
    --exclude '.DS_Store' \
    --exclude '*.log' \
    "$source_dir" "$target_dir"
}

copy_file() {
  local source_file="$1"
  local target_file="$2"

  if [[ -f "$source_file" ]]; then
    install -m 644 "$source_file" "$target_file"
  fi
}

copy_tree "$ROOT_DIR/JasEatsChoiceJava/" "$STAGE_DIR/01_source_code/JasEatsChoiceJava/"
copy_tree "$ROOT_DIR/JasEatsChoiceFront/" "$STAGE_DIR/01_source_code/JasEatsChoiceFront/"
copy_tree "$ROOT_DIR/JasEatsChoiceUniApp/" "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/"
copy_tree "$ROOT_DIR/tests/" "$STAGE_DIR/01_source_code/tests/"
copy_tree "$ROOT_DIR/database/" "$STAGE_DIR/01_source_code/database/"
copy_tree "$ROOT_DIR/docker/" "$STAGE_DIR/01_source_code/docker/"
copy_tree "$ROOT_DIR/scripts/" "$STAGE_DIR/01_source_code/scripts/"

rm -f "$STAGE_DIR/01_source_code/scripts/package_delivery_bundle.sh"
rm -f "$STAGE_DIR/01_source_code/scripts/package_thesis_attachment.sh"

copy_file "$ROOT_DIR/README.md" "$STAGE_DIR/01_source_code/README.md"
copy_file "$ROOT_DIR/package.json" "$STAGE_DIR/01_source_code/package.json"
copy_file "$ROOT_DIR/package-lock.json" "$STAGE_DIR/01_source_code/package-lock.json"
copy_file "$ROOT_DIR/.env.example" "$STAGE_DIR/01_source_code/.env.example"

copy_tree "$ROOT_DIR/docs/delivery/" "$STAGE_DIR/00_delivery_docs/"
copy_file "$ROOT_DIR/docs/product/佳食宜选.md" "$STAGE_DIR/02_project_docs/项目概述.md"
copy_file "$ROOT_DIR/docs/product/PRD.md" "$STAGE_DIR/02_project_docs/产品需求说明书.md"
copy_file "$ROOT_DIR/docs/api/后端API文档.md" "$STAGE_DIR/02_project_docs/后端API文档.md"
copy_file "$ROOT_DIR/docs/testing/test-report.md" "$STAGE_DIR/02_project_docs/自动化测试报告.md"
copy_file "$ROOT_DIR/docs/testing/test-checklist.md" "$STAGE_DIR/02_project_docs/自动化测试清单.md"
copy_file "$ROOT_DIR/docs/dev-logs/项目实现与测试覆盖现状_20260420.md" "$STAGE_DIR/02_project_docs/项目实现与测试覆盖现状.md"

copy_file "$ROOT_DIR/jia_shi_yi_xuan_backup_20260314_142416.sql" "$STAGE_DIR/03_database_scripts/full-database-backup.sql"
copy_tree "$ROOT_DIR/database/migrations/" "$STAGE_DIR/03_database_scripts/migrations/"
copy_tree "$ROOT_DIR/JasEatsChoiceJava/sql/" "$STAGE_DIR/03_database_scripts/java-sql/"
copy_file "$ROOT_DIR/JasEatsChoiceJava/food_nutrition.sql" "$STAGE_DIR/03_database_scripts/nutrition/food_nutrition.sql"
copy_file "$ROOT_DIR/JasEatsChoiceJava/src/main/resources/sql/nutrition_table.sql" "$STAGE_DIR/03_database_scripts/nutrition/nutrition_table.sql"

scrub_file() {
  local file_path="$1"
  shift

  if [[ -f "$file_path" ]]; then
    perl -0pi -e "$*" "$file_path"
  fi
}

scrub_file "$STAGE_DIR/01_source_code/JasEatsChoiceFront/src/renderer/src/config/index.js" \
  "s/const devAmapKeyFallback = '.*?'/const devAmapKeyFallback = ''/g; s/const devAmapSecurityJsCodeFallback = '.*?'/const devAmapSecurityJsCodeFallback = ''/g"
scrub_file "$STAGE_DIR/01_source_code/JasEatsChoiceFront/.env.local" \
  "s/VITE_AMAP_KEY=.*/VITE_AMAP_KEY=请替换为自己的高德地图Key/g; s/VITE_AMAP_SECURITY_JS_CODE=.*/VITE_AMAP_SECURITY_JS_CODE=请替换为自己的高德安全密钥/g"
scrub_file "$STAGE_DIR/01_source_code/JasEatsChoiceJava/src/main/resources/application.yml" \
  "s#path: /Users/[^\\n]*/uploads/#path: ./uploads/#g; s/jwt:\\n  secret: [^\\n]*/jwt:\\n  secret: 请替换为32位以上随机字符串/g; s/api-key: [0-9a-f]{32}/api-key: 请替换为自己的高德地图Key/g; s/key: [0-9a-f]{32}/key: 请替换为自己的高德地图Key/g; s/key: [A-Z0-9]{5}(-[A-Z0-9]{5}){3,}/key: 请替换为自己的腾讯地图Key/g; s/username: [^\\n@]+\\@[^\\n]+/username: your_email@example.com/g; s/password: [a-z]{16}\\n/password: 请替换为邮箱授权码\\n/g"
scrub_file "$STAGE_DIR/01_source_code/JasEatsChoiceJava/src/main/resources/application-local.yml" \
  "s#path: /Users/[^\\n]*/uploads/#path: ./uploads/#g"
scrub_file "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/src/config/index.js" \
  "s/appId: 'wx[^']+'/appId: '请替换为自己的微信小程序AppID'/g"
scrub_file "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/src/manifest.json" \
  "s/\"appid\" : \"wx[^\"]+\"/\"appid\" : \"请替换为自己的微信小程序AppID\"/g; s/\"appid\": \"wx[^\"]+\"/\"appid\": \"请替换为自己的微信小程序AppID\"/g"
scrub_file "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/manifest.json" \
  "s/\"appid\" : \"wx[^\"]+\"/\"appid\" : \"请替换为自己的微信小程序AppID\"/g; s/\"appid\": \"wx[^\"]+\"/\"appid\": \"请替换为自己的微信小程序AppID\"/g"
scrub_file "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/project.config.json" \
  "s/\"appid\": \"wx[^\"]+\"/\"appid\": \"请替换为自己的微信小程序AppID\"/g"
scrub_file "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/.env.development" \
  "s/wx[0-9a-zA-Z]\\{10,\\}/请替换为自己的微信小程序AppID/g"
scrub_file "$STAGE_DIR/01_source_code/scripts/generate_api_doc.py" \
  "s#/Users/nickxiao/JasEatsChoice#./JasEatsChoice#g"
scrub_file "$STAGE_DIR/01_source_code/scripts/playwright_capture_thesis_screenshots.js" \
  "s#/Users/nickxiao/.codex/worktrees/[^']*/JasEatsChoice#./JasEatsChoice#g"
scrub_file "$STAGE_DIR/01_source_code/JasEatsChoiceFront/REFACTOR_GUIDE.md" \
  "s#/Users/nickxiao/JasEatsChoice/#./#g"

find "$STAGE_DIR" -name '.DS_Store' -delete

(
  cd "$STAGE_DIR"
  find . -type f | sed 's#^\./##' | sort > "$STAGE_DIR/00_delivery_docs/自动生成目录清单.txt"
)

find "$STAGE_DIR" -name '.DS_Store' -delete

cd "$OUTPUT_DIR"
zip -qr "$ZIP_PATH" "$PACKAGE_NAME"
find "$STAGE_DIR" -name '.DS_Store' -delete

echo "交付目录: $STAGE_DIR"
echo "压缩包: $ZIP_PATH"
du -sh "$STAGE_DIR" "$ZIP_PATH"
