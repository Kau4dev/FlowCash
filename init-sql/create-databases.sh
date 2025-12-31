#!/bin/bash
set -e

echo "🚀 Criando bancos de dados para os microservices..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Criar bancos
    CREATE DATABASE ms_user;
    CREATE DATABASE ms_wallet;
    CREATE DATABASE ms_transfer;

    -- Garantir permissões
    GRANT ALL PRIVILEGES ON DATABASE ms_user TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE ms_wallet TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE ms_transfer TO $POSTGRES_USER;
EOSQL

echo "✅ Bancos criados com sucesso!"
echo "   - ms_user"
echo "   - ms_wallet"
echo "   - ms_transfer"
