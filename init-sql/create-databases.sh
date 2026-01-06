#!/bin/bash
set -e

echo "🚀 Criando bancos de dados para os microservices..."

# Usar postgres como usuario padrao se POSTGRES_USER nao estiver definido
USER=${POSTGRES_USER:-postgres}

psql -v ON_ERROR_STOP=1 --username "$USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Criar bancos
    CREATE DATABASE ms_user;
    CREATE DATABASE ms_wallet;
    CREATE DATABASE ms_transfer;

    -- Garantir permissoes
    GRANT ALL PRIVILEGES ON DATABASE ms_user TO $USER;
    GRANT ALL PRIVILEGES ON DATABASE ms_wallet TO $USER;
    GRANT ALL PRIVILEGES ON DATABASE ms_transfer TO $USER;
EOSQL

echo "✅ Bancos criados com sucesso!"
echo "   - ms_user"
echo "   - ms_wallet"
echo "   - ms_transfer"
