#!/bin/bash

echo "Preparing for Heroku deployment..."

# Build frontend first
echo "Building frontend..."
cd Frontend
# Copy all frontend files to backend static directory
mkdir -p ../Backend/src/main/resources/static
cp -r *.html css js assets images ../Backend/src/main/resources/static/

# Build backend
echo "Building backend..."
cd ../Backend
./gradlew clean build -x test

echo "Build complete! Ready for deployment."
echo ""
echo "To deploy to Heroku:"
echo "1. Create a Heroku app: heroku create your-app-name"
echo "2. Add PostgreSQL: heroku addons:create heroku-postgresql:hobby-dev"
echo "3. Set environment variables:"
echo "   heroku config:set SPRING_PROFILES_ACTIVE=heroku"
echo "   heroku config:set JWT_SECRET=your-secret-key"
echo "   heroku config:set OPENAI_API_KEY=your-openai-key"
echo "   heroku config:set CORS_ALLOWED_ORIGINS=https://your-app-name.herokuapp.com"
echo "4. Deploy: git push heroku main"
