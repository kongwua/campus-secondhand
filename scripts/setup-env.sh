#!/bin/bash
set -e

echo ""
echo "======================================"
echo "  Campus Secondhand Environment"
echo "======================================"
echo ""

echo "-> Checking Java..."
if command -v java &>/dev/null; then
    java -version 2>&1 | head -1
    echo "✓ Java installed"
else
    echo "✗ Java not installed"
    echo "  Install: brew install openjdk@21 (macOS)"
    echo "  Install: apt install openjdk-21-jdk (Linux)"
fi

echo ""
echo "-> Checking Maven..."
if command -v mvn &>/dev/null; then
    mvn -version 2>&1 | head -1
    echo "✓ Maven installed"
else
    echo "✗ Maven not installed"
    echo "  Install: brew install maven (macOS)"
    echo "  Install: apt install maven (Linux)"
fi

echo ""
echo "-> Checking Node.js..."
if command -v node &>/dev/null; then
    node --version
    echo "✓ Node.js installed"
else
    echo "✗ Node.js not installed"
    echo "  Install: brew install node (macOS)"
    echo "  Install: apt install nodejs (Linux)"
fi

echo ""
echo "-> Checking npm..."
if command -v npm &>/dev/null; then
    npm --version
    echo "✓ npm installed"
else
    echo "✗ npm not installed"
fi

echo ""
echo "-> Checking MySQL client..."
if command -v mysql &>/dev/null; then
    mysql --version
    echo "✓ MySQL client installed"
else
    echo "⚠ MySQL client not installed (optional for local dev)"
fi

echo ""
echo "======================================"
echo "  Configuration"
echo "======================================"
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/campus-secondhand-backend"
CONFIG_FILE="$BACKEND_DIR/src/main/resources/huawei-cloud.yml"
EXAMPLE_FILE="$BACKEND_DIR/src/main/resources/huawei-cloud-example.yml"

if [ -f "$CONFIG_FILE" ]; then
    echo "✓ huawei-cloud.yml exists"
    if grep -q "your-" "$CONFIG_FILE" 2>/dev/null; then
        echo "⚠ Contains placeholder values - please edit with real credentials"
    else
        echo "✓ Configured with real values"
    fi
else
    echo "⚠ huawei-cloud.yml not found"
    if [ -f "$EXAMPLE_FILE" ]; then
        echo "-> Copying example to config..."
        cp "$EXAMPLE_FILE" "$CONFIG_FILE"
        echo "⚠ Please edit $CONFIG_FILE with your credentials"
    fi
fi

echo ""
echo "======================================"
echo "  Project Status"
echo "======================================"
echo ""

JAR_FILE="$BACKEND_DIR/target/campus-secondhand-backend.jar"
if [ -f "$JAR_FILE" ]; then
    SIZE=$(ls -lh "$JAR_FILE" 2>/dev/null | awk '{print $5}')
    echo "✓ Backend JAR: $SIZE"
else
    echo "⚠ Backend not built - run: mvn clean package -DskipTests"
fi

FRONTEND_DIST="$PROJECT_ROOT/campus-secondhand-frontend/dist"
if [ -d "$FRONTEND_DIST" ]; then
    JS_COUNT=$(ls "$FRONTEND_DIST/assets"/*.js 2>/dev/null | wc -l | tr -d ' ')
    echo "✓ Frontend built: $JS_COUNT JS files"
else
    echo "⚠ Frontend not built - run: npm run build"
fi

echo ""
echo "======================================"
echo "  Quick Commands"
echo "======================================"
echo ""
echo "  Backend build:  cd campus-secondhand-backend && mvn clean package -DskipTests"
echo "  Backend start:  java -jar target/campus-secondhand-backend.jar"
echo "  Frontend build: cd campus-secondhand-frontend && npm run build"
echo "  Frontend dev:   npm run dev"
echo ""
echo "======================================"
echo ""