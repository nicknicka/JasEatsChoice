# Database

## What actually creates the schema

No migration tool is wired up. Flyway and Liquibase appear nowhere in
`apps/server/src/main/resources/application*.yml`, so nothing in `migrations/`
runs on boot. The live structure comes from a MySQL dump taken on 2026-03-14.

`docker compose` seeds MySQL through `/docker-entrypoint-initdb.d`, which only
executes when the `mysql-data` volume is empty:

| File | Role |
|---|---|
| `init/01_schema.sql` | `CREATE TABLE` statements only, no rows |
| `init/02_seed.sql` | demo rows: merchants, dishes, tutorials, admins, test accounts |
| `init/03_food_nutrition.sql` | nutrition reference library (`j_food_categories`, `j_food_nutrition`) |

To re-seed from scratch: `docker compose down -v && docker compose up`.

## Seed must keep these accounts

`tests/fixtures/test-base.js` logs in with them; if the seed loses a row or the
BCrypt hash no longer matches the plaintext, the Playwright suite fails at the
first test rather than the affected one.

| Role | Account | Password |
|---|---|---|
| user / merchant | `17322222222` | `asdasd` |
| admin | `admin` | `admin123` |

Regenerate an admin hash with `apps/server/tools/GenerateAdminPassword.java`
and write the result into `init/02_seed.sql`.

Real end-user phones and emails in the dump are scrubbed; log and session tables
are emptied. PII never enters this repo.

## Encoding

The dump was produced under a mismatched client charset, so comment text went
through UTF-8 twice and some rows read as mojibake. Both guard rails are required:

- MySQL starts with `--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci`
- every `init/*.sql` opens with `SET NAMES utf8mb4;`

Verify after seeding with `python3 scripts/audit_admin_mojibake.py`.

## `migrations/`

41 historical scripts, kept for traceability only. Two naming schemes coexist
(`V2026...__desc.sql` from Flyway conventions, `20260212_desc.sql` written by
hand) and their content is already folded into the dump. Do not apply them to a
seeded database.
