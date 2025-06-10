# Connection Test Guide

## 1. Backend Server Status Check
```bash
# Check if backend is running
curl -X GET http://localhost:8080/api/test/health

# Test authentication
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test3@example.com","password":"Test1234!"}'
```

## 2. Frontend Server Status Check
```bash
# Check if frontend is accessible
curl -I http://localhost:3000

# Or with python server
curl -I http://localhost:8000
```

## 3. Common Issues and Solutions

### Issue: CORS Error
**Solution**: Backend CORS is configured for ports 3000 and 8000. Make sure you're using one of these ports for frontend.

### Issue: 401 Unauthorized
**Solution**: Token might be expired or missing. Try logging in again.

### Issue: 404 Not Found
**Solution**: Check if the API endpoint exists in the backend controllers.

### Issue: Connection Refused
**Solution**: Make sure both backend (8080) and frontend (3000/8000) servers are running.

## 4. Test API Endpoints

### Study Plans
```bash
# Get all study plans (requires auth token)
curl -X GET http://localhost:8080/api/study-plans \
  -H "Authorization: Bearer YOUR_TOKEN"

# Create a study plan
curl -X POST http://localhost:8080/api/study-plans \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Study Session",
    "type": "study",
    "date": "2024-12-06",
    "startTime": "14:00",
    "endTime": "16:00",
    "allDay": false,
    "color": "#667eea"
  }'
```

### Materials Upload
```bash
# Upload a material (requires auth token and a file)
curl -X POST http://localhost:8080/api/materials/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test.pdf" \
  -F "title=Test Material" \
  -F "className=CS101"
```
