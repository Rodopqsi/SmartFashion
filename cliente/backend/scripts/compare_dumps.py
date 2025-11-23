#!/usr/bin/env python3
import re
import sys
import json

def parse_create_tables(sql_text):
    s = sql_text
    pattern = re.compile(r"CREATE\s+TABLE\s+`?(?P<name>[0-9A-Za-z_]+)`?\s*\((?P<body>.*?)\)\s*(ENGINE|DEFAULT|;)", re.S | re.I)
    tables = {}
    for m in pattern.finditer(s):
        name = m.group('name')
        body = m.group('body')
        cols = []
        for line in body.splitlines():
            line = line.strip().rstrip(',')
            if not line: 
                continue
            if re.match(r"^(PRIMARY KEY|UNIQUE KEY|KEY|CONSTRAINT|FOREIGN KEY|INDEX|FULLTEXT|CHECK)\b", line, re.I):
                continue
            colm = re.match(r"^`?(?P<col>[0-9A-Za-z_]+)`?\s+(?P<rest>.+)$", line)
            if colm:
                col = colm.group('col')
                rest = colm.group('rest').strip()
                rest = re.sub(r"COMMENT\s+'[^']*'", '', rest, flags=re.I)
                rest = re.sub(r"\s+", ' ', rest).strip()
                cols.append((col, rest))
        tables[name.lower()] = { 'name': name, 'columns': {c[0].lower(): c[1] for c in cols}, 'col_order': [c[0].lower() for c in cols] }
    return tables


def compare(a, b):
    ta = set(a.keys())
    tb = set(b.keys())
    only_a = sorted(list(ta - tb))
    only_b = sorted(list(tb - ta))
    common = sorted(list(ta & tb))
    diffs = {}
    for t in common:
        cols_a = a[t]['columns']
        cols_b = b[t]['columns']
        ca = set(cols_a.keys())
        cb = set(cols_b.keys())
        only_col_a = sorted(list(ca - cb))
        only_col_b = sorted(list(cb - ca))
        col_common = sorted(list(ca & cb))
        col_diffs = []
        for c in col_common:
            if normalize_type(cols_a[c]) != normalize_type(cols_b[c]):
                col_diffs.append({'column': c, 'a': cols_a[c], 'b': cols_b[c]})
        if only_col_a or only_col_b or col_diffs:
            diffs[t] = { 'only_in_a': only_col_a, 'only_in_b': only_col_b, 'mismatched_columns': col_diffs }
    return only_a, only_b, diffs


def normalize_type(t):
    t = t.lower()
    t = t.replace('unsigned', ' unsigned ')
    t = re.sub(r"\bcharacter set\b.*?$", '', t)
    t = re.sub(r"\bdefault charset\b.*?$", '', t)
    t = re.sub(r"collate\s+[^\s]+", '', t)
    t = re.sub(r"\s+", ' ', t).strip()
    return t

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print('Usage: compare_dumps.py dumpA.sql dumpB.sql')
        sys.exit(2)
    a_path = sys.argv[1]
    b_path = sys.argv[2]
    a_text = open(a_path, 'r', encoding='utf-8', errors='ignore').read()
    b_text = open(b_path, 'r', encoding='utf-8', errors='ignore').read()
    ta = parse_create_tables(a_text)
    tb = parse_create_tables(b_text)
    only_a, only_b, diffs = compare(ta, tb)
    out = {
        'file_a': a_path,
        'file_b': b_path,
        'tables_only_in_a': only_a,
        'tables_only_in_b': only_b,
        'table_diffs': diffs
    }
    print(json.dumps(out, indent=2, ensure_ascii=False))
