#!/bin/bash

# Debug script for Studia servers

echo "=== Studia Server Debug Script ==="
echo "=================================="

# Function to check if port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        echo "❌ Port $port is already in use!"
        echo "Process using port $port:"
        lsof -i :$port
        return 1
    else
        echo "✅ Port $port is available"
        return 0
    fi
}

# Check required ports
echo ""
echo "1. Checking ports..."
check_port 8080
BACKEND_PORT_FREE=$?
check_port 3000
FRONTEND_PORT_FREE=$?

# Check MySQL
echo ""
echo "2. Checking MySQL..."
if mysqladmin ping -h localhost -u root --silent; then
    echo "✅ MySQL is running"
    
    # Check if database exists
    if mysql -u root -e "USE studia_db" 2>/dev/null; then
        echo "✅ Database 'studia_db' exists"
    else
        echo "❌ Database 'studia_db' does not exist"
        echo "Creating database..."
        mysql -u root -e "CREATE DATABASE studia_db;"
        mysql -u root -e "CREATE USER IF NOT EXISTS 'studia_user'@'localhost' IDENTIFIED BY 'studia_password';"
        mysql -u root -e "GRANT ALL PRIVILEGES ON studia_db.* TO 'studia_user'@'localhost';"
        mysql -u root -e "FLUSH PRIVILEGES;"
        echo "✅ Database created"
    fi
else
    echo "❌ MySQL is not running"
    echo "Please start MySQL first: brew services start mysql"
fi

# Check backend environment
echo ""
echo "3. Checking backend environment..."
if [ -f "Backend/.env" ]; then
    echo "✅ Backend .env file exists"
else
    echo "❌ Backend .env file missing"
    echo "Creating .env file..."
    cat > Backend/.env << EOF
OPENAI_API_KEY=your-openai-api-key-here
DB_USERNAME=studia_user
DB_PASSWORD=studia_password
EOF
    echo "⚠️  Please update OPENAI_API_KEY in Backend/.env"
fi

# Kill existing processes if needed
echo ""
echo "4. Cleanup..."
if [ $BACKEND_PORT_FREE -eq 1 ]; then
    echo "Killing process on port 8080..."
    kill -9 $(lsof -ti:8080) 2>/dev/null
fi
if [ $FRONTEND_PORT_FREE -eq 1 ]; then
    echo "Killing process on port 3000..."
    kill -9 $(lsof -ti:3000) 2>/dev/null
fi

# Start servers
echo ""
echo "5. Starting servers..."
echo "=================================="

# Start backend
echo "Starting backend server..."
cd Backend
nohup ./gradlew bootRun > backend.log 2>&1 &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"
cd ..

# Wait for backend to start
echo "Waiting for backend to start..."
sleep 10

# Check if backend is running
if curl -s http://localhost:8080/api/test/health > /dev/null; then
    echo "✅ Backend is running"
else
    echo "❌ Backend failed to start. Check Backend/backend.log"
    tail -50 Backend/backend.log
    exit 1
fi

# Start frontend
echo ""
echo "Starting frontend server..."
cd Frontend
nohup python3 server.py > frontend.log 2>&1 &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"
cd ..

# Wait for frontend to start
sleep 3

# Check if frontend is running
if curl -s http://localhost:3000 > /dev/null; then
    echo "✅ Frontend is running"
else
    echo "❌ Frontend failed to start. Check Frontend/frontend.log"
    tail -50 Frontend/frontend.log
    exit 1
fi

echo ""
echo "=================================="
echo "✅ Both servers are running!"
echo ""
echo "Access the application at: http://localhost:3000"
echo ""
echo "To stop servers:"
echo "  kill $BACKEND_PID   # Stop backend"
echo "  kill $FRONTEND_PID  # Stop frontend"
echo ""
echo "To view logs:"
echo "  tail -f Backend/backend.log    # Backend logs"
echo "  tail -f Frontend/frontend.log  # Frontend logs"
echo ""
echo "Test login credentials:"
echo "  Email: test3@example.com"
echo "  Password: Test1234!"
echo "=================================="
