@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: Campus Secondhand Platform - Environment Setup Script (Windows)

:: Colors (Windows)
for /F %%a in ('echo prompt $E^| cmd') do set "ESC=%%a"
set "RED=!ESC![31m"
set "GREEN=!ESC![32m"
set "YELLOW=!ESC![33m"
set "BLUE=!ESC![34m"
set "NC=!ESC![0m"

:: Project directories
set "PROJECT_ROOT=%~dp0.."
set "BACKEND_DIR=%PROJECT_ROOT%\campus-secondhand-backend"
set "FRONTEND_DIR=%PROJECT_ROOT%\campus-secondhand-frontend"

:: Version requirements
set "JAVA_MIN_VERSION=21"
set "NODE_MIN_VERSION=18"

:: Main menu
if "%1"=="" goto :help
if "%1"=="check" goto :check
if "%1"=="install" goto :install
if "%1"=="config" goto :config
if "%1"=="database" goto :database
if "%1"=="deps" goto :deps
if "%1"=="build" goto :build
if "%1"=="full" goto :full
if "%1"=="help" goto :help
goto :unknown

:header
echo.
echo %BLUE%======================================%NC%
echo %BLUE%  %~1%NC%
echo %BLUE%======================================%NC%
echo.
goto :eof

:success
echo %GREEN%✓ %~1%NC%
goto :eof

:error
echo %RED%✗ %~1%NC%
goto :eof

:warning
echo %YELLOW%⚠ %~1%NC%
goto :eof

:info
echo %BLUE%→ %~1%NC%
goto :eof

:check
call :header "Environment Detection"
set "all_ok=1"

:: Check Java
call :info "Checking Java..."
where java >nul 2>&1
if %ERRORLEVEL%==0 (
    for /F "tokens=3" %%a in ('java -version 2^>^&1 ^| findstr /i "version"') do set "java_version=%%a"
    call :info "Found: !java_version!"
    call :success "Java installed"
) else (
    call :error "Java not installed"
    set "all_ok=0"
)

:: Check Maven
call :info "Checking Maven..."
where mvn >nul 2>&1
if %ERRORLEVEL%==0 (
    for /F "tokens=3" %%a in ('mvn -version ^| findstr /i "Apache Maven"') do set "mvn_version=%%a"
    call :info "Found: Maven !mvn_version!"
    call :success "Maven installed"
) else (
    call :error "Maven not installed"
    set "all_ok=0"
)

:: Check Node.js
call :info "Checking Node.js..."
where node >nul 2>&1
if %ERRORLEVEL%==0 (
    for /F %%a in ('node --version') do set "node_version=%%a"
    call :info "Found: Node.js !node_version!"
    call :success "Node.js installed"
) else (
    call :error "Node.js not installed"
    set "all_ok=0"
)

:: Check npm
call :info "Checking npm..."
where npm >nul 2>&1
if %ERRORLEVEL%==0 (
    for /F %%a in ('npm --version') do set "npm_version=%%a"
    call :info "Found: npm !npm_version!"
    call :success "npm installed"
) else (
    call :error "npm not installed"
    set "all_ok=0"
)

echo.
if "!all_ok!"=="1" (
    call :success "All required tools installed!"
) else (
    call :warning "Some tools missing. Run 'setup-env.bat install' to install."
)
goto :eof

:install
call :header "Installing Dependencies (Windows)"

call :info "For Windows, please install manually:"
echo.
echo   1. Java JDK 21+: https://adoptium.net/
echo   2. Maven: https://maven.apache.org/download.cgi
echo   3. Node.js 18+: https://nodejs.org/
echo.
echo Or use package managers:
echo   - winget install EclipseAdoptium.Temurin.21.JDK
echo   - winget install Apache.Maven
echo   - winget install OpenJS.NodeJS.LTS
echo.
call :warning "After installation, restart terminal and run 'setup-env.bat check'"
goto :eof

:config
call :header "Configuration Setup"

set "config_file=%BACKEND_DIR%\src\main\resources\huawei-cloud.yml"
set "example_file=%BACKEND_DIR%\src\main\resources\huawei-cloud-example.yml"

if exist "!config_file!" (
    call :success "huawei-cloud.yml exists"
    findstr "your-" "!config_file!" >nul
    if %ERRORLEVEL%==0 (
        call :warning "Config contains placeholder values"
        call :info "Edit !config_file! with actual credentials"
    ) else (
        call :success "huawei-cloud.yml configured"
    )
) else (
    call :warning "huawei-cloud.yml not found"
    if exist "!example_file!" (
        call :info "Copying example to config..."
        copy "!example_file!" "!config_file!" >nul
        call :warning "Edit !config_file! with your credentials"
    ) else (
        call :error "huawei-cloud-example.yml not found"
    )
)
goto :eof

:database
call :header "Database Initialization"

set "schema_file=%PROJECT_ROOT%\sql\schema.sql"
set "seed_file=%PROJECT_ROOT%\sql\seed.sql"

if not exist "!schema_file!" (
    call :error "schema.sql not found"
    goto :eof
)

echo.
echo %YELLOW%Database Configuration:%NC%
set /p "DB_HOST=MySQL Host [localhost]: "
if "!DB_HOST!"=="" set "DB_HOST=localhost"
set /p "DB_PORT=MySQL Port [3306]: "
if "!DB_PORT!"=="" set "DB_PORT=3306"
set /p "DB_NAME=Database Name [campus]: "
if "!DB_NAME!"=="" set "DB_NAME=campus"
set /p "DB_USER=MySQL Username [root]: "
if "!DB_USER!"=="" set "DB_USER=root"
set /p "DB_PASS=MySQL Password: "

call :info "Creating database..."
mysql -h!DB_HOST! -P!DB_PORT! -u!DB_USER! -p!DB_PASS! -e "CREATE DATABASE IF NOT EXISTS !DB_NAME! CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci" 2>nul
if %ERRORLEVEL%==0 (
    call :success "Database created"
    call :info "Importing schema..."
    mysql -h!DB_HOST! -P!DB_PORT! -u!DB_USER! -p!DB_PASS! !DB_NAME! < "!schema_file!"
    call :success "Schema imported"
    if exist "!seed_file!" (
        call :info "Importing seed data..."
        mysql -h!DB_HOST! -P!DB_PORT! -u!DB_USER! -p!DB_PASS! !DB_NAME! < "!seed_file!"
        call :success "Seed data imported"
    )
) else (
    call :error "Database operation failed"
)
goto :eof

:deps
call :header "Frontend Dependencies"
cd /d "%FRONTEND_DIR%"
if exist "package.json" (
    call :info "Installing npm dependencies..."
    npm install
    call :success "Frontend dependencies installed"
) else (
    call :error "package.json not found"
)
cd /d "%PROJECT_ROOT%"
goto :eof

:build
call :header "Building Project"

:: Build backend
cd /d "%BACKEND_DIR%"
if exist "pom.xml" (
    call :info "Building backend..."
    mvn clean package -DskipTests
    if exist "target\campus-secondhand-backend.jar" (
        call :success "Backend built: target\campus-secondhand-backend.jar"
    ) else (
        call :error "Backend build failed"
    )
) else (
    call :error "pom.xml not found"
)

:: Build frontend
cd /d "%FRONTEND_DIR%"
call :info "Building frontend..."
npm run build
if exist "dist" (
    call :success "Frontend built"
) else (
    call :error "Frontend build failed"
)
cd /d "%PROJECT_ROOT%"
goto :eof

:full
call :check
call :config
echo.
set /p "init_db=Initialize database? [y/N]: "
if "!init_db!"=="y" call :database
call :deps
call :build

call :header "Setup Complete"
echo.
echo %GREEN%Environment setup successful!%NC%
echo.
echo Next steps:
echo   1. Edit %BACKEND_DIR%\src\main\resources\huawei-cloud.yml
echo   2. Start backend:  java -jar %BACKEND_DIR%\target\campus-secondhand-backend.jar
echo   3. Start frontend: cd %FRONTEND_DIR% && npm run dev
echo.
goto :eof

:help
echo Campus Secondhand Platform - Environment Setup Script (Windows)
echo.
echo Usage: setup-env.bat [command]
echo.
echo Commands:
echo   check     Check environment
echo   install   Show installation instructions
echo   config    Check/create config files
echo   database  Initialize database
echo   deps      Install frontend dependencies
echo   build     Build backend and frontend
echo   full      Complete setup
echo   help      Show this help
echo.
goto :eof

:unknown
call :error "Unknown command: %1"
call :help
goto :eof