#!/usr/bin/env python3
import sys
p=sys.argv[1]
s=open(p,'rb').read()
subs = [b'aplicacion_promocion', b'CREATE TABLE', b'CREATE TABLE `']
for sub in subs:
    idx=s.find(sub)
    print('search for', sub, 'found at', idx)
    if idx!=-1:
        start=max(0, idx-60)
        end=idx+120
        print(s[start:end])
print('first 200 bytes (hex):', s[:200].hex())
