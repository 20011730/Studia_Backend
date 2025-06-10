#!/bin/bash

# 백엔드 실행 스크립트
# 메모리 설정과 함께 Spring Boot 실행

echo "Starting Studia Backend Server..."
echo "================================"

# JVM 메모리 설정
export JAVA_OPTS="-Xms256m -Xmx512m"

# 환경 변수 확인
if [ ! -f .env ]; then
    echo "Error: .env file not found!"
    echo "Please copy .env.example to .env and set your API keys."
    exit 1
fi

# 환경 변수 로드
export $(cat .env | grep -v '^#' | xargs)

echo "Environment variables loaded"
echo "Starting server on port 8080..."

# Gradle 실행
./gradlew bootRun

