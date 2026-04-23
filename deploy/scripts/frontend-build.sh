#!/bin/bash
# Frontend build script for Huawei Cloud ECS
set -euo pipefail

APP_DIR="/opt/campus-secondhand/campus-secondhand-frontend"
WWW_DIR="/var/www/campus-secondhand/frontend"

mkdir -p $WWW_DIR

cd $APP_DIR

# Install dependencies
npm install

# Build production
npm run build

# Deploy to www
cp -r dist/* $WWW_DIR/

# Set permissions
chmod -R 755 $WWW_DIR

echo "Frontend built and deployed to $WWW_DIR"