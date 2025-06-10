#!/bin/bash

# 프론트엔드 실행 스크립트

echo "Starting Studia Frontend Server..."
echo "================================="

# Python 버전 확인
python3 --version

echo ""
echo "Server will run on http://localhost:3000"
echo "Press Ctrl+C to stop the server"
echo ""

# 서버 실행
python3 server.py
