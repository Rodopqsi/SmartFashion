try:
    import pymysql  # type: ignore
    pymysql.install_as_MySQLdb()
except Exception:
    # Si mysqlclient está instalado, no es necesario PyMySQL
    pass
