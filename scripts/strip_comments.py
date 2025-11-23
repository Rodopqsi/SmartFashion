#!/usr/bin/env python3
"""
Safe comment stripper.
- Skips `app_movil`, `node_modules`, `target`, `build`, `dist`.
- Skips files larger than 2MB and known SQL dump files like db_dump_full and drawSQL-mysql-export.
- Preserves comments that include keywords: TODO, FIXME, COPYRIGHT, LICENSE, @license, #! (shebang), encoding comments.
- Backs up original files under `scripts/backups/<timestamp>`.
- Produces `scripts/modified_files.txt` listing changed files.

Use carefully and review diffs before committing.
"""
import os, sys, re, shutil, time
from pathlib import Path

ROOT = Path(r'C:/SmarthFashion')
EXCLUDE_DIRS = ['app_movil', 'node_modules', 'target', 'build', 'dist', '.git']
MAX_SIZE = 2 * 1024 * 1024
BACKUP_DIR = Path(__file__).resolve().parent / 'backups' / time.strftime('%Y%m%d_%H%M%S')
MODIFIED_LIST = Path(__file__).resolve().parent / 'modified_files.txt'

EXT_LANG = {
    '.py': 'python',
    '.js': 'js', '.jsx': 'js', '.ts': 'js', '.tsx': 'js',
    '.java': 'cstyle', '.c': 'cstyle', '.cpp': 'cstyle', '.h': 'cstyle',
    '.css': 'cstyle', '.scss': 'cstyle',
    '.html': 'html', '.htm': 'html', '.xml': 'html',
    '.sql': 'sql', '.properties': 'props', '.env': 'props', '.sh': 'sh', '.ps1': 'ps1'
}

PRESERVE_KEYWORDS = ['TODO', 'FIXME', 'COPYRIGHT', 'LICENSE', '@license', ']#!']

re_c_block = re.compile(r'/\*.*?\*/', re.S)
re_c_line = re.compile(r'//.*?$' , re.M)
re_html_comment = re.compile(r'<!--.*?-->', re.S)
re_py_line = re.compile(r'(^\s*#.*$)', re.M)
re_sql_line = re.compile(r'(^\s*--.*$)', re.M)
re_props_line = re.compile(r'(^\s*[^=\n]*)#.*$', re.M)

def contains_preserve(text):
    up = text.upper()
    for k in PRESERVE_KEYWORDS:
        if k.upper() in up:
            return True
    # encoding comment like -*- coding: utf-8 -*-
    if 'coding' in up or 'ENCODING' in up:
        return True
    return False


def process_file(path: Path):
    ext = path.suffix.lower()
    lang = EXT_LANG.get(ext)
    if not lang:
        return False
    size = path.stat().st_size
    if size > MAX_SIZE:
        return False
    text = path.read_text(encoding='utf-8', errors='ignore')
    orig = text
    changed = False
    if lang == 'python':
        lines = text.splitlines(True)
        out = []
        for ln in lines:
            if ln.lstrip().startswith('#'):
                if contains_preserve(ln):
                    out.append(ln)
                else:
                    changed = True
                continue
            if '# ' in ln:
                parts = ln.split('# ', 1)
                code, comment = parts[0], parts[1]
                if contains_preserve(comment):
                    out.append(ln)
                else:
                    out.append(code.rstrip() + '\n')
                    if code.rstrip() != ln.rstrip():
                        changed = True
            else:
                out.append(ln)
        text = ''.join(out)
    elif lang == 'sql':
        def repl_sql_line(m):
            ln = m.group(0)
            if contains_preserve(ln):
                return ln
            else:
                return ''
        text2 = re_sql_line.sub(repl_sql_line, text)
        text2 = re_c_block.sub(lambda m: m.group(0) if contains_preserve(m.group(0)) else '', text2)
        if text2 != text:
            changed = True
        text = text2
    elif lang == 'html':
        def repl_html(m):
            c = m.group(0)
            return c if contains_preserve(c) else ''
        text2 = re_html_comment.sub(repl_html, text)
        if text2 != text:
            changed = True
        text = text2
    elif lang == 'props' or lang == 'sh' or lang == 'ps1':
        # lines starting with # (or inline # after key) — preserve TODO etc
        def repl_props(m):
            ln = m.group(0)
            return ln if contains_preserve(ln) else ''
        text2 = re_props_line.sub(repl_props, text)
        if text2 != text:
            changed = True
        text = text2
    elif lang == 'js' or lang == 'cstyle':
        def repl_block(m):
            s = m.group(0)
            return s if contains_preserve(s) else ''
        text2 = re_c_block.sub(repl_block, text)
        def repl_line(m):
            s = m.group(0)
            return s if contains_preserve(s) else ''
        text2 = re_c_line.sub(repl_line, text2)
        if text2 != text:
            changed = True
        text = text2
    else:
        return False

    if changed and text != orig:
        target_backup = BACKUP_DIR / path.relative_to(ROOT)
        target_backup.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(path, target_backup)
        path.write_text(text, encoding='utf-8')
        return True
    return False


if __name__ == '__main__':
    modified = []
    BACKUP_DIR.mkdir(parents=True, exist_ok=True)
    for root, dirs, files in os.walk(ROOT):
        parts = Path(root).parts
        if any(x in parts for x in EXCLUDE_DIRS):
            continue
        for f in files:
            p = Path(root) / f
            if 'backups' in p.parts:
                continue
            if 'db_dump_full' in p.name or 'drawSQL-mysql-export' in p.name:
                continue
            try:
                if process_file(p):
                    modified.append(str(p))
            except Exception as e:
                print('ERROR processing', p, e)

    MODIFIED_LIST.write_text('\n'.join(modified), encoding='utf-8')
    print('Done. Modified', len(modified), 'files. Backups in', BACKUP_DIR)

