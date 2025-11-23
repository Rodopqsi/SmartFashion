#!/usr/bin/env python3
import sys
p=sys.argv[1]
with open(p,'r',encoding='utf-8',errors='ignore') as f:
    for i,line in enumerate(f):
        if 'CREATE TABLE' in line.upper():
            print(line.strip())
            # print next line for context
            try: print(next(f).strip())
            except StopIteration: pass
            if i>100: break
