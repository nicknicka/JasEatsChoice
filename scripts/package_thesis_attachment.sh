#!/bin/zsh

# Legacy packager: paths match the archived repo layout (JasEatsChoiceJava/ JasEatsChoiceFront/ docker/ database/), not apps/ here.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
DATE_TAG="$(date +%Y%m%d)"
PACKAGE_NAME="JasEatsChoice_Thesis_Attachment_${DATE_TAG}"
OUTPUT_DIR="$ROOT_DIR/output/thesis-package"
STAGE_DIR="$OUTPUT_DIR/$PACKAGE_NAME"
ZIP_PATH="$OUTPUT_DIR/${PACKAGE_NAME}.zip"

mkdir -p "$OUTPUT_DIR"
rm -rf "$STAGE_DIR" "$ZIP_PATH"

mkdir -p \
  "$STAGE_DIR/00_attachment_note" \
  "$STAGE_DIR/01_source_code" \
  "$STAGE_DIR/02_project_docs" \
  "$STAGE_DIR/03_thesis_materials"

copy_tree() {
  local source_dir="$1"
  local target_dir="$2"

  rsync -a \
    --exclude '.git/' \
    --exclude '.github/' \
    --exclude '.idea/' \
    --exclude '.vscode/' \
    --exclude '.claude/' \
    --exclude '.artifacts/' \
    --exclude '.env.local' \
    --exclude 'node_modules/' \
    --exclude 'target/' \
    --exclude 'dist/' \
    --exclude 'out/' \
    --exclude 'unpackage/' \
    --exclude 'uploads/' \
    --exclude 'reports/' \
    --exclude '.DS_Store' \
    --exclude '*.log' \
    "$source_dir" "$target_dir"
}

copy_file() {
  local source_file="$1"
  local target_file="$2"

  install -m 644 "$source_file" "$target_file"
}

copy_tree "$ROOT_DIR/JasEatsChoiceJava/" "$STAGE_DIR/01_source_code/JasEatsChoiceJava/"
copy_tree "$ROOT_DIR/JasEatsChoiceFront/" "$STAGE_DIR/01_source_code/JasEatsChoiceFront/"
copy_tree "$ROOT_DIR/JasEatsChoiceUniApp/" "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/"
copy_tree "$ROOT_DIR/tests/" "$STAGE_DIR/01_source_code/tests/"
copy_tree "$ROOT_DIR/database/" "$STAGE_DIR/01_source_code/database/"
copy_tree "$ROOT_DIR/docker/" "$STAGE_DIR/01_source_code/docker/"
copy_tree "$ROOT_DIR/scripts/" "$STAGE_DIR/01_source_code/scripts/"
rm -f "$STAGE_DIR/01_source_code/scripts/package_thesis_attachment.sh"

perl -0pi -e "s/const devAmapKeyFallback = '.*?'/const devAmapKeyFallback = ''/g; s/const devAmapSecurityJsCodeFallback = '.*?'/const devAmapSecurityJsCodeFallback = ''/g" \
  "$STAGE_DIR/01_source_code/JasEatsChoiceFront/src/renderer/src/config/index.js"
perl -0pi -e "s/password:\\s*123456/password: change_me/g" \
  "$STAGE_DIR/01_source_code/JasEatsChoiceJava/src/main/resources/application.yml" \
  "$STAGE_DIR/01_source_code/JasEatsChoiceJava/src/main/resources/application-local.yml"
perl -0pi -e 's/jwt:\n  secret: [^\n]*/jwt:\n  secret: your_jwt_secret_here/g; s/api-key: [0-9a-f]{32}/api-key: your_amap_api_key_here/g; s/key: [0-9a-f]{32}/key: your_amap_api_key_here/g; s/key: [A-Z0-9]{5}(-[A-Z0-9]{5}){3,}/key: your_tencent_map_key_here/g; s/username: [^\n@]+\@[^\n]+/username: your_email\@example.com/g; s/password: [a-z]{16}\n/password: your_email_auth_code_here\n/g' \
  "$STAGE_DIR/01_source_code/JasEatsChoiceJava/src/main/resources/application.yml"
perl -0pi -e "s/DB_PASSWORD=123456/DB_PASSWORD=change_me/g; s/MYSQL_ROOT_PASSWORD=123456/MYSQL_ROOT_PASSWORD=change_me/g; s/-p123456/-pchange_me/g" \
  "$STAGE_DIR/01_source_code/docker/docker-compose.dev.yml"
perl -0pi -e "s/\"appid\": \"wx[^\"]+\"/\"appid\": \"your_wechat_appid_here\"/g; s/\"appid\" : \"wx[^\"]+\"/\"appid\" : \"your_wechat_appid_here\"/g; s/appId: 'wx[^']+'/appId: 'your_wechat_appid_here'/g" \
  "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/project.config.json" \
  "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/manifest.json" \
  "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/src/manifest.json" \
  "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/src/config/index.js"
perl -0pi -e "s/wx[0-9a-zA-Z]{10,}/your_wechat_appid_here/g; s/[A-Z0-9]{5}(-[A-Z0-9]{5}){3,}/your_tencent_map_key_here/g" \
  "$STAGE_DIR/01_source_code/JasEatsChoiceUniApp/配置指南.md" \
  "$STAGE_DIR/01_source_code/JasEatsChoiceJava/src/test/java/com/xx/jaseatschoicejava/service/impl/LocationServiceImplTest.java"

copy_file "$ROOT_DIR/README.md" "$STAGE_DIR/01_source_code/README.md"
copy_file "$ROOT_DIR/.env.example" "$STAGE_DIR/01_source_code/.env.example"
copy_file "$ROOT_DIR/package.json" "$STAGE_DIR/01_source_code/package.json"
copy_file "$ROOT_DIR/package-lock.json" "$STAGE_DIR/01_source_code/package-lock.json"

if [[ -f "$ROOT_DIR/jia_shi_yi_xuan_backup_20260314_142416.sql" ]]; then
  copy_file \
    "$ROOT_DIR/jia_shi_yi_xuan_backup_20260314_142416.sql" \
    "$STAGE_DIR/01_source_code/full-database-backup.sql"
fi

copy_file "$ROOT_DIR/docs/thesis/毕设附件打包说明.md" "$STAGE_DIR/00_attachment_note/attachment-packaging-note.md"

copy_file "$ROOT_DIR/docs/product/佳食宜选.md" "$STAGE_DIR/02_project_docs/project-overview.md"
copy_file "$ROOT_DIR/docs/product/PRD.md" "$STAGE_DIR/02_project_docs/product-requirements.md"
copy_file "$ROOT_DIR/docs/api/后端API文档.md" "$STAGE_DIR/02_project_docs/backend-api.md"
copy_file "$ROOT_DIR/docs/dev-logs/项目实现与测试覆盖现状_20260420.md" "$STAGE_DIR/02_project_docs/implementation-and-test-status.md"
copy_file "$ROOT_DIR/docs/testing/test-report.md" "$STAGE_DIR/02_project_docs/automated-test-report.md"
copy_file "$ROOT_DIR/docs/testing/test-checklist.md" "$STAGE_DIR/02_project_docs/automated-test-checklist.md"
copy_file "$ROOT_DIR/docs/中期检查报告.md" "$STAGE_DIR/02_project_docs/midterm-report.md"
copy_file "$ROOT_DIR/docs/毕业设计指导记录_十次完整内容.md" "$STAGE_DIR/02_project_docs/supervision-records.md"

copy_file "$ROOT_DIR/docs/thesis/毕业论文初稿.docx" "$STAGE_DIR/03_thesis_materials/thesis-draft.docx"
copy_file "$ROOT_DIR/docs/thesis/thesis_refined_diagrams.docx" "$STAGE_DIR/03_thesis_materials/thesis-diagrams.docx"
copy_file "$ROOT_DIR/docs/2026年毕业设计-指导情况记录表-十次完整填写版.docx" "$STAGE_DIR/03_thesis_materials/advisor-record-form.docx"

cd "$OUTPUT_DIR"
zip -qr "$ZIP_PATH" "$PACKAGE_NAME"

echo "附件目录: $STAGE_DIR"
echo "压缩包: $ZIP_PATH"
du -sh "$STAGE_DIR" "$ZIP_PATH"
