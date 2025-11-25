# Instrucciones para limpiar e importar el dump de la base de datos

Este directorio incluye un pequeño script `clean_db_dump.sh` que genera una copia limpia del dump SQL original eliminando cualquier cabecera no-SQL (por ejemplo, "Enter password:").

Pasos (en la VM `/opt/smartfashion`):

1. Hacer `git pull` para traer los cambios (el script) al servidor:

```bash
cd /opt/smartfashion
git pull origin main
```

2. Ejecutar el script para crear el archivo limpio (ajusta la ruta si tu dump está en otra ubicación):

```bash
cd /opt/smartfashion/cliente/backend
chmod +x clean_db_dump.sh
./clean_db_dump.sh db_dump_full.utf8.sql db_dump_full.utf8.cleaned.sql
```

Esto generará `db_dump_full.utf8.cleaned.sql` en el mismo directorio.

3. Importar el dump limpio en el contenedor MySQL (ajusta el nombre del contenedor y la contraseña si difieren):

```bash
# copia el archivo al contenedor (opcional)
sudo docker cp db_dump_full.utf8.cleaned.sql deploy-db-1:/tmp/db_dump_full.utf8.cleaned.sql

# importar directamente desde la VM hacia mysql (forma robusta)
sudo docker exec -i deploy-db-1 sh -c 'mysql -uroot -prodo2006 smarthfashion' < db_dump_full.utf8.cleaned.sql

# verificar que las tablas existen
sudo docker exec -i deploy-db-1 sh -c "mysql -uroot -prodo2006 -e 'SHOW TABLES IN smarthfashion;'"
```

4. Reiniciar servicios (opcional pero recomendado):

```bash
sudo docker compose up -d --no-deps --force-recreate db backend admin proxy
```

Notas:
- Si tu contraseña root de MySQL no es `rodo2006`, reemplázala en los comandos anteriores.
- Si el dump contiene otras líneas basura más complejas, ejecuta `sed -n '1,80p' db_dump_full.utf8.sql` y pega la salida en el issue para que podamos ajustar el limpiador.
