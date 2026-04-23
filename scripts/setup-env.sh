#!/bin/bash

# ===================================
# Campus Secondhand Platform
# Environment Setup Script
# ===================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/campus-secondhand-backend"
FRONTEND_DIR="$PROJECT_ROOT/campus-secondhand-frontend"

# Version requirements
JAVA_MIN_VERSION=21
MAVEN_MIN_VERSION=3.8
NODE_MIN_VERSION=18
NPM_MIN_VERSION=9

# ===================================
# Helper Functions
# ===================================

print_header() {
    echo -e "\n${BLUE}======================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}======================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}→ $1${NC}"
}

check_command() {
    if command -v "$1" &> /dev/null; then
        return 0
    else
        return 1
    fi
}

get_version() {
    local cmd="$1"
    local version_flag="$2"
    local parse_pattern="$3"
    
    if check_command "$cmd"; then
        $cmd "$version_flag" 2>&1 | grep -oE "$parse_pattern" | head -1
    else
        echo "not installed"
    fi
}

compare_version() {
    # Returns 0 if installed >= required, 1 otherwise
    local installed="$1"
    local required="$2"
    
    if [[ "$installed" == "not installed" ]]; then
        return 1
    fi
    
    # Simple numeric comparison for major version
    local installed_major=$(echo "$installed" | cut -d. -f1)
    local required_major=$(echo "$required" | cut -d. -f1)
    
    if [[ "$installed_major" -ge "$required_major" ]]; then
        return 0
    else
        return 1
    fi
}

# ===================================
# Environment Detection
# ===================================

detect_java() {
    print_info "Checking Java..."
    local version=$(get_version "java" "-version" "[0-9]+")
    
    if check_command "java"; then
        local full_version=$(java -version 2>&1 | head -1)
        print_info "Found: $full_version"
        
        if compare_version "$version" "$JAVA_MIN_VERSION"; then
            print_success "Java $version (≥ $JAVA_MIN_VERSION required)"
            return 0
        else
            print_warning "Java $version found, but $JAVA_MIN_VERSION+ required"
            return 1
        fi
    else
        print_error "Java not installed"
        return 1
    fi
}

detect_maven() {
    print_info "Checking Maven..."
    local version=$(get_version "mvn" "-version" "[0-9]+\.[0-9]")
    
    if check_command "mvn"; then
        print_info "Found: Maven $version"
        
        if compare_version "$version" "$MAVEN_MIN_VERSION"; then
            print_success "Maven $version (≥ $MAVEN_MIN_VERSION required)"
            return 0
        else
            print_warning "Maven $version found, but $MAVEN_MIN_VERSION+ required"
            return 1
        fi
    else
        print_error "Maven not installed"
        return 1
    fi
}

detect_node() {
    print_info "Checking Node.js..."
    local version=$(get_version "node" "--version" "[0-9]+")
    
    if check_command "node"; then
        print_info "Found: Node.js $(node --version)"
        
        if compare_version "$version" "$NODE_MIN_VERSION"; then
            print_success "Node.js $version (≥ $NODE_MIN_VERSION required)"
            return 0
        else
            print_warning "Node.js $version found, but $NODE_MIN_VERSION+ required"
            return 1
        fi
    else
        print_error "Node.js not installed"
        return 1
    fi
}

detect_npm() {
    print_info "Checking npm..."
    local version=$(get_version "npm" "--version" "[0-9]+")
    
    if check_command "npm"; then
        print_info "Found: npm $version"
        
        if compare_version "$version" "$NPM_MIN_VERSION"; then
            print_success "npm $version (≥ $NPM_MIN_VERSION required)"
            return 0
        else
            print_warning "npm $version found, but $NPM_MIN_VERSION+ required"
            return 1
        fi
    else
        print_error "npm not installed"
        return 1
    fi
}

detect_mysql() {
    print_info "Checking MySQL client..."
    
    if check_command "mysql"; then
        local version=$(mysql --version 2>&1 | grep -oE "[0-9]+\.[0-9]+" | head -1)
        print_success "MySQL client $version found"
        return 0
    else
        print_warning "MySQL client not installed (optional for local dev)"
        return 0  # Optional, don't fail
    fi
}

# ===================================
# Installation Functions
# ===================================

install_java_macos() {
    print_info "Installing Java via Homebrew..."
    if check_command "brew"; then
        brew install openjdk@21
        # Symlink if needed
        sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk 2>/dev/null || true
        print_success "Java installed"
    else
        print_error "Homebrew not found. Please install Homebrew first: https://brew.sh"
        return 1
    fi
}

install_java_linux() {
    print_info "Installing Java..."
    if check_command "apt-get"; then
        sudo apt-get update
        sudo apt-get install -y openjdk-21-jdk
        print_success "Java installed"
    elif check_command "yum"; then
        sudo yum install -y java-21-openjdk-devel
        print_success "Java installed"
    else
        print_error "No supported package manager found"
        return 1
    fi
}

install_maven_macos() {
    print_info "Installing Maven via Homebrew..."
    brew install maven
    print_success "Maven installed"
}

install_maven_linux() {
    print_info "Installing Maven..."
    if check_command "apt-get"; then
        sudo apt-get update
        sudo apt-get install -y maven
        print_success "Maven installed"
    elif check_command "yum"; then
        sudo yum install -y maven
        print_success "Maven installed"
    else
        print_error "No supported package manager found"
        return 1
    fi
}

install_node_macos() {
    print_info "Installing Node.js via Homebrew..."
    brew install node
    print_success "Node.js installed"
}

install_node_linux() {
    print_info "Installing Node.js..."
    # Use NodeSource for latest version
    if check_command "apt-get"; then
        curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
        sudo apt-get install -y nodejs
        print_success "Node.js installed"
    elif check_command "yum"; then
        curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
        sudo yum install -y nodejs
        print_success "Node.js installed"
    else
        print_error "No supported package manager found"
        return 1
    fi
}

get_os_type() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "macos"
    elif [[ "$OSTYPE" == "linux"* ]]; then
        echo "linux"
    else
        echo "unknown"
    fi
}

# ===================================
# Project Configuration
# ===================================

check_config_files() {
    print_info "Checking configuration files..."
    
    local config_file="$BACKEND_DIR/src/main/resources/huawei-cloud.yml"
    local example_file="$BACKEND_DIR/src/main/resources/huawei-cloud-example.yml"
    
    if [[ -f "$config_file" ]]; then
        print_success "huawei-cloud.yml exists"
        
        # Check if it has real values (not placeholders)
        if grep -q "your-" "$config_file"; then
            print_warning "huawei-cloud.yml contains placeholder values"
            print_info "Please edit $config_file with your actual credentials"
            return 1
        else
            print_success "huawei-cloud.yml configured"
            return 0
        fi
    else
        print_warning "huawei-cloud.yml not found"
        
        if [[ -f "$example_file" ]]; then
            print_info "Copying example file to config file..."
            cp "$example_file" "$config_file"
            print_warning "Please edit $config_file with your actual credentials:"
            print_info "  - RDS host, username, password"
            print_info "  - OBS access-key, secret-key, bucket"
            return 1
        else
            print_error "huawei-cloud-example.yml not found"
            return 1
        fi
    fi
}

init_database() {
    print_info "Initializing database..."
    
    local schema_file="$PROJECT_ROOT/sql/schema.sql"
    local seed_file="$PROJECT_ROOT/sql/seed.sql"
    
    if [[ ! -f "$schema_file" ]]; then
        print_error "schema.sql not found"
        return 1
    fi
    
    # Prompt for database credentials
    echo ""
    echo -e "${YELLOW}Database Configuration:${NC}"
    read -p "MySQL Host [localhost]: " DB_HOST
    DB_HOST=${DB_HOST:-localhost}
    
    read -p "MySQL Port [3306]: " DB_PORT
    DB_PORT=${DB_PORT:-3306}
    
    read -p "Database Name [campus]: " DB_NAME
    DB_NAME=${DB_NAME:-campus}
    
    read -p "MySQL Username [root]: " DB_USER
    DB_USER=${DB_USER:-root}
    
    read -s -p "MySQL Password: " DB_PASS
    echo ""
    
    # Check connection
    print_info "Testing database connection..."
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1" &>/dev/null; then
        print_success "Database connection successful"
    else
        print_error "Database connection failed"
        return 1
    fi
    
    # Create database if not exists
    print_info "Creating database '$DB_NAME'..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
    print_success "Database created"
    
    # Import schema
    print_info "Importing schema..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$schema_file"
    print_success "Schema imported"
    
    # Import seed data
    if [[ -f "$seed_file" ]]; then
        print_info "Importing seed data..."
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$seed_file"
        print_success "Seed data imported"
    fi
    
    print_success "Database initialization complete"
}

install_frontend_deps() {
    print_info "Installing frontend dependencies..."
    
    cd "$FRONTEND_DIR"
    
    if [[ -f "package.json" ]]; then
        npm install
        print_success "Frontend dependencies installed"
    else
        print_error "package.json not found"
        return 1
    fi
    
    cd "$PROJECT_ROOT"
}

build_backend() {
    print_info "Building backend..."
    
    cd "$BACKEND_DIR"
    
    if [[ -f "pom.xml" ]]; then
        mvn clean package -DskipTests
        print_success "Backend built successfully"
        
        # Check if jar was created
        if [[ -f "target/campus-secondhand-backend.jar" ]]; then
            print_success "JAR file created: target/campus-secondhand-backend.jar"
        else
            print_error "JAR file not found after build"
            return 1
        fi
    else
        print_error "pom.xml not found"
        return 1
    fi
    
    cd "$PROJECT_ROOT"
}

build_frontend() {
    print_info "Building frontend..."
    
    cd "$FRONTEND_DIR"
    
    if npm run build; then
        print_success "Frontend built successfully"
        
        if [[ -d "dist" ]]; then
            print_success "Dist folder created"
        else
            print_error "Dist folder not found"
            return 1
        fi
    else
        print_error "Frontend build failed"
        return 1
    fi
    
    cd "$PROJECT_ROOT"
}

# ===================================
# Main Functions
# ===================================

run_detection() {
    print_header "Environment Detection"
    
    local all_ok=true
    
    detect_java || all_ok=false
    detect_maven || all_ok=false
    detect_node || all_ok=false
    detect_npm || all_ok=false
    detect_mysql
    
    echo ""
    
    if $all_ok; then
        print_success "All required tools are installed!"
        return 0
    else
        print_warning "Some required tools are missing"
        return 1
    fi
}

run_installation() {
    print_header "Installing Missing Components"
    
    local os=$(get_os_type)
    print_info "Operating System: $os"
    
    # Check and install Java
    if ! detect_java; then
        print_info "Installing Java..."
        case $os in
            macos) install_java_macos ;;
            linux) install_java_linux ;;
            *) print_error "Unsupported OS: $os"; return 1 ;;
        esac
    fi
    
    # Check and install Maven
    if ! detect_maven; then
        print_info "Installing Maven..."
        case $os in
            macos) install_maven_macos ;;
            linux) install_maven_linux ;;
            *) print_error "Unsupported OS: $os"; return 1 ;;
        esac
    fi
    
    # Check and install Node.js (includes npm)
    if ! detect_node; then
        print_info "Installing Node.js..."
        case $os in
            macos) install_node_macos ;;
            linux) install_node_linux ;;
            *) print_error "Unsupported OS: $os"; return 1 ;;
        esac
    fi
    
    print_header "Installation Complete"
    run_detection
}

run_full_setup() {
    print_header "Full Environment Setup"
    
    # Step 1: Install dependencies
    run_installation
    
    # Step 2: Check/create config
    print_header "Configuration Setup"
    check_config_files || true
    
    # Step 3: Initialize database (optional)
    echo ""
    read -p "Initialize database now? [y/N]: " init_db
    if [[ "$init_db" == "y" || "$init_db" == "Y" ]]; then
        init_database
    fi
    
    # Step 4: Install frontend deps
    print_header "Frontend Dependencies"
    install_frontend_deps
    
    # Step 5: Build everything
    print_header "Building Project"
    build_backend
    build_frontend
    
    print_header "Setup Complete!"
    
    echo ""
    echo -e "${GREEN}Environment setup successful!${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Edit $BACKEND_DIR/src/main/resources/huawei-cloud.yml with your credentials"
    echo "  2. Start backend:  java -jar $BACKEND_DIR/target/campus-secondhand-backend.jar"
    echo "  3. Start frontend: cd $FRONTEND_DIR && npm run dev"
    echo ""
}

show_help() {
    echo "Campus Secondhand Platform - Environment Setup Script"
    echo ""
    echo "Usage: ./setup-env.sh [command]"
    echo ""
    echo "Commands:"
    echo "  check     Check environment (detect installed tools)"
    echo "  install   Install missing dependencies"
    echo "  config    Check/create configuration files"
    echo "  database  Initialize database"
    echo "  deps      Install frontend npm dependencies"
    echo "  build     Build backend and frontend"
    echo "  full      Run full setup (install + config + build)"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./setup-env.sh check    # Just check what's installed"
    echo "  ./setup-env.sh full     # Complete setup"
    echo ""
}

# ===================================
# Entry Point
# ===================================

case "${1:-}" in
    check|--check|-c)
        run_detection
        ;;
    install|--install|-i)
        run_installation
        ;;
    config|--config)
        check_config_files
        ;;
    database|--database|-d)
        init_database
        ;;
    deps|--deps)
        install_frontend_deps
        ;;
    build|--build|-b)
        build_backend
        build_frontend
        ;;
    full|--full|-f)
        run_full_setup
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        if [[ -n "${1:-}" ]]; then
            print_error "Unknown command: $1"
            echo ""
            show_help
            exit 1
        else
            # Default: show help
            show_help
        fi
        ;;
esac