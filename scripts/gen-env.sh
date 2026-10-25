#!/usr/bin/env bash
# Generate deploy/.env from deploy/.env.example with freshly randomised secrets.
# Re-running only fills blanks, so already-set values survive.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TEMPLATE="$REPO_ROOT/deploy/.env.example"
TARGET="$REPO_ROOT/deploy/.env"

if [[ ! -f "$TEMPLATE" ]]; then
  echo "missing template: $TEMPLATE" >&2
  exit 1
fi

random() {
  # 32 url-safe chars from openssl; strip padding so it survives YAML/compose quoting.
  openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | cut -c1-32
}

if [[ ! -f "$TARGET" ]]; then
  cp "$TEMPLATE" "$TARGET"
  chmod 600 "$TARGET"
  echo "created $TARGET"
else
  echo "existing $TARGET kept; filling blank secret slots only"
fi

for var in MYSQL_ROOT_PASSWORD DB_PASSWORD REDIS_PASSWORD JWT_SECRET; do
  current="$(grep -E "^${var}=" "$TARGET" | tail -1 | cut -d= -f2- || true)"
  case "$current" in
    ""|change_me*)
      value="$(random)"
      perl -0pi -e "s/^${var}=.*/${var}=\Q${value}\E/m" "$TARGET"
      echo "set ${var}"
      ;;
    *) echo "kept ${var}" ;;
  esac
done

echo
echo "Next: put real values in $TARGET for ZHIPU_API_KEY, AMAP_API_KEY, TENCENT_MAP_KEY, MAIL_USERNAME, MAIL_PASSWORD."
echo "This file is git-ignored; never commit it."
