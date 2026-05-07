@echo off
echo ================================
echo ATS Resume Checker - Setup
echo ================================
echo.

echo Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install Java 17 or higher.
    pause
    exit /b 1
)
echo [OK] Java found

echo Checking Maven installation...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven not found. Please install Maven 3.6+
    pause
    exit /b 1
)
echo [OK] Maven found

echo Checking Node.js installation...
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js not found. Please install Node.js 16+
    pause
    exit /b 1
)
echo [OK] Node.js found

echo Checking npm installation...
npm -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] npm not found. Please install npm
    pause
    exit /b 1
)
echo [OK] npm found

echo.
echo All prerequisites satisfied!
echo.

echo Setting up Backend...
cd backend
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Backend setup failed
    cd ..
    pause
    exit /b 1
)
echo [OK] Backend setup complete
cd ..

echo.
echo Setting up Frontend...
cd frontend
call npm install
if %errorlevel% neq 0 (
    echo [ERROR] Frontend setup failed
    cd ..
    pause
    exit /b 1
)
echo [OK] Frontend setup complete
cd ..

echo.
echo ================================
echo Setup Complete!
echo ================================
echo.
echo To start the application:
echo.
echo 1. Start Backend (in Command Prompt 1):
echo    cd backend
echo    mvn spring-boot:run
echo.
echo 2. Start Frontend (in Command Prompt 2):
echo    cd frontend
echo    npm run dev
echo.
echo 3. Open browser: http://localhost:5173
echo ================================
pause
