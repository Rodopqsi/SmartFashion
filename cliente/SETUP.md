# SmarthFashion Cliente - Setup Rápido

## Backend (Django + PyMySQL)

1. Crear entorno virtual:
```powershell
python -m venv .venv
. .venv\Scripts\Activate.ps1
```
2. Instalar dependencias:
```powershell
pip install -r backend\requirements.txt
```
3. Variables de entorno (ajusta credenciales):
```powershell
$env:MYSQL_DATABASE = "smarthfashion"
$env:MYSQL_USER = "root"
$env:MYSQL_PASSWORD = ""
$env:MYSQL_HOST = "127.0.0.1"
$env:MYSQL_PORT = "3306"
```
4. Ejecutar servidor:
```powershell
python backend\manage.py runserver 8000
```
Abrir: http://127.0.0.1:8000/api/home/

### ¿Necesitas mysqlclient?
`PyMySQL` basta para desarrollo. Si quieres `mysqlclient` (más rápido):
1. Instala Microsoft C++ Build Tools (Visual Studio Installer) con "Desarrollo de escritorio con C++".
2. Instala MySQL Connector/C (o MariaDB Connector/C) y agrega include/lib a PATH.
3. Añade la línea `mysqlclient==2.2.4` de nuevo a `requirements.txt` y ejecuta `pip install -r backend\requirements.txt`.

## Frontend (React + Vite)
```powershell
cd frontend
npm install
npm run dev
```
Abrir: http://localhost:5173

Si el backend no corre en 127.0.0.1:8000, crea `frontend/.env` con:
```
VITE_API_BASE=http://TU_HOST:PUERTO
```

## Prueba rápida API vía curl (PowerShell)
```powershell
curl http://127.0.0.1:8000/api/home/
```

## Próximos pasos sugeridos
- Añadir cálculo de promociones.
- Paginación y filtros (categoría, búsqueda).
- Tests (pytest o unittest) para el endpoint.
- Manejo de imágenes y media estática.
