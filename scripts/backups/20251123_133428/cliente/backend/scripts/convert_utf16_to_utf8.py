#!/usr/bin/env python3
import sys
p=sys.argv[1]
out=sys.argv[2]
# Try common utf encodings
for enc in ('utf-8','utf-16','utf-16-le','utf-16-be','latin-1'):
    try:
        s=open(p,'r',encoding=enc,errors='strict').read()
        open(out,'w',encoding='utf-8',errors='strict').write(s)
        print('converted',p,'from',enc,'to',out)
        break
    except Exception as e:
        # try next
        last=e
print('done')
