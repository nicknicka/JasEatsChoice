#!/usr/bin/env python3
"""Turn the live jia_shi_yi_xuan database (mounted inside the jx-extract container)
into a repo-safe seed: repair double-encoded text, mask personal data, drop logs."""
import json
import subprocess
import sys

CJK = "[一-鿿]"
MOJI_CHARS = "[À-ÿŒœŠšŸŽžƒˆ˜–—‘’“”•…‰′″‹›€¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¹º»¼½¾¿]"


def sql(statement):
    out = subprocess.run(
        ["docker", "exec", "jx-extract", "mysql", "-uroot",
         "--default-character-set=utf8mb4", "--silent", "-e", statement],
        capture_output=True, text=True)
    if out.returncode:
        print("SQL ERROR:", out.stderr.strip()[:400], file=sys.stderr)
        sys.exit(1)
    return out.stdout.strip()


def text_columns():
    q = ("select table_name, column_name, data_type from information_schema.columns "
         "where table_schema='jia_shi_yi_xuan' and table_name not like 'v\\_%' "
         "and data_type in ('varchar','char','text','tinytext','mediumtext','longtext','json')")
    rows = [l.split("\t") for l in sql(q).split("\n") if l.strip()]
    return [(t, c, dt) for t, c, dt in rows]


def repaired(c):
    return f"CONVERT(CAST(CONVERT(`{c}` USING latin1) AS BINARY) USING utf8mb4)"


def count(statement):
    v = sql(statement)
    return sum(int(l) for l in v.split("\n") if l.strip().isdigit())


def scan_predicate(t, c):
    return (f"select count(*) from `jia_shi_yi_xuan`.`{t}` where `{c}` regexp '{MOJI_CHARS}{MOJI_CHARS}' "
            f"and `{c}` not regexp '{CJK}' and char_length({repaired(c)}) < char_length(`{c}`) "
            f"and {repaired(c)} not regexp '{MOJI_CHARS}{MOJI_CHARS}'")


def update_predicate(t, c):
    return (f"`{c}` regexp '{MOJI_CHARS}{MOJI_CHARS}' and `{c}` not regexp '{CJK}' "
            f"and char_length({repaired(c)}) < char_length(`{c}`) "
            f"and {repaired(c)} not regexp '{MOJI_CHARS}{MOJI_CHARS}'")


def count_scan(preds, chunk=40):
    total = 0
    for i in range(0, len(preds), chunk):
        total += count("union all ".join(preds[i:i + chunk]) + ";")
    return total


def main():
    cols = text_columns()
    print(f"[scan] {len(cols)} text columns")
    plain = [(t, c) for t, c, dt in cols if dt != "json"]
    preds = [scan_predicate(t, c) for t, c in plain]
    print(f"[scan] repairable cells: {count_scan(preds)}")

    stmts = [
        f"UPDATE IGNORE `jia_shi_yi_xuan`.`{t}` SET `{c}`={repaired(c)} WHERE {update_predicate(t, c)};"
        for t, c in plain]
    for i in range(0, len(stmts), 25):
        sql("\n".join(stmts[i:i + 25]))
    print(f"[repair] remaining double-encoded cells: {count_scan(preds)}")

    json_cols = [(t, c) for t, c, dt in cols if dt == "json"]
    for t, c in json_cols:
        as_text = f"CAST(`{c}` AS CHAR)"
        fix = f"CONVERT(CAST(CONVERT({as_text} USING latin1) AS BINARY) USING utf8mb4)"
        stmt = (f"UPDATE IGNORE `jia_shi_yi_xuan`.`{t}` SET `{c}`=CAST({fix} AS JSON) "
                f"WHERE {as_text} regexp '{MOJI_CHARS}{MOJI_CHARS}' and JSON_VALID(`{c}`)")
        out = subprocess.run(
            ["docker", "exec", "jx-extract", "mysql", "-uroot",
             "--default-character-set=utf8mb4", "-e", f"USE jia_shi_yi_xuan;\n{stmt};"],
            capture_output=True, text=True)
        if out.returncode:
            print(f"[json] skipped {t}.{c}: {out.stderr.strip()[:120]}")
    print(f"[json] swept {len(json_cols)} json columns")

    mixed = count_scan([
        f"select count(*) from `jia_shi_yi_xuan`.`{t}` where `{c}` regexp '{MOJI_CHARS}{MOJI_CHARS}' "
        f"and `{c}` regexp '{CJK}'"
        for t, c in plain])
    print(f"[scan] rows mixing CJK with double-encoded text (left untouched): {mixed}")

    h = "$2a$10$HzKwbVHKIANfyd3QUJf3zu1ya0Kh2zOhzQL0JSnsTEQs6UgBkukBe"   # asdasd
    a = "$2a$10$yonWPluz1EfcjRYndMOp0uNDnq6KGezdkyzQnvvY6O2vIRAnYCxBG"   # admin123
    sql(f"""USE jia_shi_yi_xuan;
UPDATE t_user SET password='{h}', payment_password=NULL, has_payment_password=0;
UPDATE t_merchant SET password='{h}';
UPDATE t_admin SET password='{a}', phone='13800000000', email='admin@example.com', real_name='系统管理员';
UPDATE t_user SET phone='13800138010', nickname='演示用户A', email='demoA@example.com' WHERE user_id='4590322501655448';
UPDATE t_user SET phone='13800138011', nickname='演示用户B', email='demoB@example.com' WHERE user_id='5829380186175252';
UPDATE t_user SET phone='13800138012', nickname='演示用户C', email='demoC@example.com' WHERE user_id='9689304274958108';
UPDATE t_user SET phone='13800138013', nickname='演示用户D', email='demoD@example.com' WHERE user_id='8686348687636632';
UPDATE t_user SET phone='13800138014', nickname='演示用户E', email='demoE@example.com' WHERE phone='19986175252';
UPDATE t_mock_oauth_user SET nickname='演示用户', avatar_url='';
UPDATE t_address SET receiver_name='演示收件人', receiver_phone='13800000001', detail='演示路 1 号';""")
    print("[mask] passwords and personal data rewritten")

    logs = ["recommendation_log", "user_behavior", "t_system_log", "t_admin_operation_log",
            "t_dish_step_history", "t_scheduled_task_log", "t_user_custom_event",
            "t_user_recommend_history", "t_message_record", "dish_similarity",
            "t_extraction_task"]
    kept = {t: sql(f"select count(*) from jia_shi_yi_xuan.{t}") for t in logs}
    sql("USE jia_shi_yi_xuan; " + " ".join(f"TRUNCATE TABLE {t};" for t in logs))
    sql("USE jia_shi_yi_xuan; DROP TABLE IF EXISTS tutorial_backup_20250129; DROP TABLE IF EXISTS tmp_id_mappings;")
    print("[logs] truncated:", json.dumps({k: int(v) for k, v in kept.items()}))

    print("[verify]")
    print(sql("""select 'users' k, count(*) n from jia_shi_yi_xuan.t_user
union all select 'dishes', count(*) from jia_shi_yi_xuan.t_dish
union all select 'merchants', count(*) from jia_shi_yi_xuan.t_merchant
union all select 'tutorials', count(*) from jia_shi_yi_xuan.tutorial
union all select 'recipes', count(*) from jia_shi_yi_xuan.t_recipe
union all select 'test_user', count(*) from jia_shi_yi_xuan.t_user where phone='17322222222'
union all select 'admin', count(*) from jia_shi_yi_xuan.t_admin where username='admin'"""))


if __name__ == "__main__":
    main()
