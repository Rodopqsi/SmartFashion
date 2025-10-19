#!/usr/bin/env python
"""Wrapper que reenvía a backend/manage.py para simplificar comandos.

Permite ejecutar desde la carpeta cliente:

    python manage.py migrate
    python manage.py runserver 8000

Sin tener que escribir backend\manage.py.
"""
import os
import runpy
import sys
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent
BACKEND_MANAGE = BASE_DIR / 'backend' / 'manage.py'

if not BACKEND_MANAGE.exists():
    sys.stderr.write("No se encontró backend/manage.py.\n")
    sys.exit(1)

# Ajustar cwd para que Django lo tome igual que antes
os.chdir(BACKEND_MANAGE.parent)
sys.path.insert(0, str(BACKEND_MANAGE.parent))

runpy.run_path(str(BACKEND_MANAGE), run_name='__main__')
