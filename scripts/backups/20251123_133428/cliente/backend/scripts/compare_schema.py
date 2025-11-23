#!/usr/bin/env python3
"""
Compare schema (tables + columns) between two MySQL databases and print differences.
Requires: pip install pymysql
Usage:
  python compare_schema.py --host localhost --user root --db1 smarthfashion --db2 smarthfashion_copy
"""
import argparse
import pymysql
import json


def load_schema(conn, db):
    cur = conn.cursor(pymysql.cursors.DictCursor)
    cur.execute("SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, EXTRA FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=%s ORDER BY TABLE_NAME, ORDINAL_POSITION", (db,))
    rows = cur.fetchall()
    schema = {}
    for r in rows:
        t = r['TABLE_NAME']
        schema.setdefault(t, [])
        schema[t].append({
            'column': r['COLUMN_NAME'],
            'type': r['COLUMN_TYPE'],
            'nullable': r['IS_NULLABLE'],
            'key': r['COLUMN_KEY'],
            'extra': r['EXTRA']
        })
    return schema


def diff_schema(s1, s2):
    all_tables = set(s1.keys()) | set(s2.keys())
    diffs = {}
    for t in sorted(all_tables):
        c1 = {c['column']: c for c in s1.get(t, [])}
        c2 = {c['column']: c for c in s2.get(t, [])}
        cols_all = set(c1.keys()) | set(c2.keys())
        table_diffs = []
        for col in sorted(cols_all):
            a = c1.get(col)
            b = c2.get(col)
            if not a:
                table_diffs.append({'column': col, 'status': 'missing_in_db1', 'db2': b})
            elif not b:
                table_diffs.append({'column': col, 'status': 'missing_in_db2', 'db1': a})
            else:
                # compare type/nullable/key
                if a['type'] != b['type'] or a['nullable'] != b['nullable'] or a['key'] != b['key'] or a['extra'] != b['extra']:
                    table_diffs.append({'column': col, 'status': 'mismatch', 'db1': a, 'db2': b})
        if table_diffs:
            diffs[t] = table_diffs
    return diffs


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--host', default='127.0.0.1')
    p.add_argument('--port', type=int, default=3306)
    p.add_argument('--user', required=True)
    p.add_argument('--password', default='', help='If empty, prompt')
    p.add_argument('--db1', required=True)
    p.add_argument('--db2', required=True)
    args = p.parse_args()

    pwd = args.password
    if not pwd:
        import getpass
        pwd = getpass.getpass(f"MySQL password for {args.user}@{args.host}: ")

    conn = pymysql.connect(host=args.host, port=args.port, user=args.user, password=pwd, charset='utf8mb4', cursorclass=pymysql.cursors.DictCursor)
    try:
        s1 = load_schema(conn, args.db1)
        s2 = load_schema(conn, args.db2)
    finally:
        conn.close()

    diffs = diff_schema(s1, s2)
    if not diffs:
        print("Schemas look identical (no column-level diffs found).")
    else:
        print(json.dumps(diffs, indent=2, ensure_ascii=False))

if __name__ == '__main__':
    main()
