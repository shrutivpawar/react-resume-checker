#!/bin/bash

echo "🚀 ATS Resume Checker - Setup Script"
echo "======================================"
echo ""

# Check Java
echo "Checking Java installation..."
if command -v java &> /dev/null; then
    java_version=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
    echo "✓ Java found: $java_version"
else
    echo "✗ Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check Maven
echo "Checking Maven installation..."
if command -v mvn &> /dev/null; then
    mvn_version=$(mvn -version | head -n 1)
    echo "✓ Maven found: $mvn_version"
else
    echo "✗ Maven not found. Please install Maven 3.6+."
    exit 1
fi

# Check Node.js
echo "Checking Node.js installation..."
if command -v node &> /dev/null; then
    node_version=$(node -v)
    echo "✓ Node.js found: $node_version"
else
    echo "✗ Node.js not found. Please install Node.js 16+."
    exit 1
fi

# Check npm
echo "Checking npm installation..."
if command -v npm &> /dev/null; then
    npm_version=$(npm -v)
    echo "✓ npm found: v$npm_version"
else
    echo "✗ npm not found. Please install npm."
    exit 1
fi

echo ""
echo "All prerequisites satisfied! ✓"
echo ""

# Setup Backend
echo "Setting up Backend..."
cd backend
mvn clean install -DskipTests
if [ $? -eq 0 ]; then
    echo "✓ Backend setup complete"
else
    echo "✗ Backend setup failed"
    exit 1
fi
cd ..

echo ""

# Setup Frontend
echo "Setting up Frontend..."
cd frontend
npm install
if [ $? -eq 0 ]; then
    echo "✓ Frontend setup complete"
else
    echo "✗ Frontend setup failed"
    exit 1
fi
cd ..

echo ""
echo "======================================"
echo "🎉 Setup Complete!"
echo ""
echo "To start the application:"
echo ""
echo "1. Start Backend (in terminal 1):"
echo "   cd backend && mvn spring-boot:run"
echo ""
echo "2. Start Frontend (in terminal 2):"
echo "   cd frontend && npm run dev"
echo ""
echo "3. Open browser: http://localhost:5173"
echo "======================================"
