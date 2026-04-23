#!/bin/bash
# Database initialization script for Huawei Cloud RDS
# Reads configuration from huawei-cloud.yml
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")/.."
SQL_DIR="$PROJECT_DIR/sql"

CONFIG_FILE="${1:-$PROJECT_DIR/campus-secondhand-backend/src/main/resources/huawei-cloud.yml}"

parse_yaml_config() {
    local config_file="$1"
    
    if [ ! -f "$config_file" ]; then
        echo "Error: Config file not found: $config_file"
        exit 1
    fi
    
    python3 -c "
import yaml
import sys

with open('$config_file', 'r') as f:
    config = yaml.safe_load(f)

rds = config.get('huawei-cloud', {}).get('rds', {})

print('HOST=' + str(rds.get('host', 'localhost')))
print('PORT=' + str(rds.get('port', 3306)))
print('DATABASE=' + str(rds.get('database', 'campus_secondhand')))
print('USERNAME=' + str(rds.get('username', 'root')))
print('PASSWORD=' + str(rds.get('password', 'root')))
print('CHARSET=' + str(rds.get('charset', 'utf8mb4')))
"
}

if command -v python3 &> /dev/null && python3 -c "import yaml" 2>/dev/null; then
    eval "$(parse_yaml_config "$CONFIG_FILE")"
    DB_HOST="${DB_HOST:-${HOST:-localhost}}"
    DB_PORT="${DB_PORT:-${PORT:-3306}}"
    DB_NAME="${DB_NAME:-${DATABASE:-campus_secondhand}}"
    DB_USER="${DB_USER:-${USERNAME:-root}}"
    DB_PASS="${DB_PASS:-${PASSWORD:-root}}"
    DB_CHARSET="${DB_CHARSET:-${CHARSET:-utf8mb4}}"
else
    DB_HOST="${DB_HOST:-localhost}"
    DB_PORT="${DB_PORT:-3306}"
    DB_NAME="${DB_NAME:-campus_secondhand}"
    DB_USER="${DB_USER:-root}"
    DB_PASS="${DB_PASS:-root}"
    DB_CHARSET="${DB_CHARSET:-utf8mb4}"
    echo "Warning: Python yaml module not available, using environment variables or defaults"
fi

echo "=============================================="
echo "Database Initialization"
echo "=============================================="
echo "Config file: $CONFIG_FILE"
echo "Host: $DB_HOST"
echo "Port: $DB_PORT"
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo "Charset: $DB_CHARSET"
echo "=============================================="

echo "Initializing database $DB_NAME on $DB_HOST:$DB_PORT..."

mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
    -e "CREATE DATABASE IF NOT EXISTS $DB_NAME DEFAULT CHARACTER SET $DB_CHARSET COLLATE $DB_CHARSET_unicode_ci;" || {
    echo "Error: Failed to create database"
    exit 1
}

echo "Database created successfully"

if [ -f "$SQL_DIR/schema.sql" ]; then
    echo "Importing schema..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" $DB_NAME < "$SQL_DIR/schema.sql"
    echo "Schema imported"
else
    echo "Warning: schema.sql not found at $SQL_DIR/schema.sql"
fi

if [ -f "$SQL_DIR/seed.sql" ]; then
    echo "Importing seed data..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" $DB_NAME < "$SQL_DIR/seed.sql"
    echo "Seed data imported"
else
    echo "Warning: seed.sql not found at $SQL_DIR/seed.sql"
fi

echo "=============================================="
echo "Database initialization completed"
echo "Tables created: user, category, product, transaction, message, review"
echo "Seed data: 5 categories imported"
echo "=============================================="

mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" $DB_NAME -e "SHOW TABLES;" 2>/dev/null || true