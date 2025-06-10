#!/bin/bash
# Studia Backend Server Startup Script

echo "Starting Studia Backend Server..."

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "Environment variables loaded"
else
    echo "Warning: .env file not found"
fi

# Check if MySQL is running
if ! command -v mysql &> /dev/null; then
    echo "MySQL is not installed"
else
    if mysql -u root -e "SELECT 1" &> /dev/null; then
        echo "MySQL is running"
    else
        echo "MySQL is not running. Please start MySQL first."
        exit 1
    fi
fi

# Clean and build
echo "Building project..."
./gradlew clean build -x test

# Run the application
echo "Starting Spring Boot application..."
java -jar build/libs/Studia-0.0.1-SNAPSHOT.jar
