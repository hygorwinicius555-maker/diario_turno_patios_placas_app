import os
import psycopg2

host = os.getenv('PGHOST', '144.22.174.248')
port = int(os.getenv('PGPORT', '5432'))
user = os.getenv('PGUSER', 'Usiminas')
password = os.getenv('PGPASSWORD', 'nQwV9Gg8Gt#sN&3q&r')
dbname = os.getenv('PGDATABASE', 'postgres')

conn = psycopg2.connect(host=host, port=port, user=user, password=password, dbname=dbname)
print('Conexao OK')
conn.close()
