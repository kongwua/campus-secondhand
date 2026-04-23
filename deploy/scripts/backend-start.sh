#!/bin/bash
# Backend start script for Huawei Cloud ECS
# Configuration is managed in huawei-cloud.yml
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/campus-secondhand/campus-secondhand-backend}"
LOG_DIR="${LOG_DIR:-/var/log/campus-secondhand}"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m}"

mkdir -p "$LOG_DIR"

cd "$APP_DIR"

if [ ! -f "target/campus-secondhand-backend.jar" ]; then
    echo "JAR not found, building..."
    mvn clean package -DskipTests
fi

pkill -f "campus-secondhand-backend.jar" || true
sleep 2

echo "Starting backend..."
nohup java $JAVA_OPTS \
    -jar target/campus-secondhand-backend.jar \
    > "$LOG_DIR/backend.log" 2>&1 &

sleep 3

PID=$(pgrep -f campus-secondhand-backend.jar 2>/dev/null || echo "")

if [ -n "$PID" ]; then
    echo "Backend started successfully. PID: $PID"
    echo "Log file: $LOG_DIR/backend.log"
    echo "Config file: src/main/resources/huawei-cloud.yml"
else
    echo "Error: Backend failed to start"
    echo "Check log: tail -f $LOG_DIR/backend.log"
    exit 1
fi