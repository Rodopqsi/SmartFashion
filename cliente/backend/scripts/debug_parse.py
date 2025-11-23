#!/usr/bin/env python3
import re, sys
pattern = re.compile(r"CREATE\s+TABLE\s+`?(?P<name>[0-9A-Za-z_]+)`?\s*\((?P<body>.*?)\)\s*(ENGINE|DEFAULT|;)", re.S | re.I)
for p in sys.argv[1:]:
    s = open(p,'r',encoding='utf-8',errors='ignore').read()
    names = [m.group('name') for m in pattern.finditer(s)]
    print(p)
    print('count:', len(names))
    print(sorted(names)[:20])
