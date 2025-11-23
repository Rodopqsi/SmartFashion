#!/usr/bin/env python3
import re, sys
p = sys.argv[1]
s = open(p, 'r', encoding='utf-8', errors='ignore').read()
names = [m.group(1) for m in re.finditer(r'CREATE\s+TABLE\s+`?([A-Za-z0-9_]+)`?', s, re.I)]
print(p)
print(len(names))
print(sorted(set([n.lower() for n in names])))
