#!/bin/bash

echo "Preparing for Heroku deployment..."

# Copy frontend files to backend static directory
echo "Copying frontend files to backend..."
mkdir -p Backend/src/main/resources/static
cp -r Frontend/*.html Backend/src/main/resources/static/
cp -r Frontend/css Backend/src/main/resources/static/
cp -r Frontend/js Backend/src/main/resources/static/
cp -r Frontend/assets Backend/src/main/resources/static/
cp -r Frontend/images Backend/src/main/resources/static/ 2>/dev/null || true
cp -r Frontend/fonts Backend/src/main/resources/static/ 2>/dev/null || true

# Build backend
echo "Building backend..."
cd Backend
./gradlew clean build -x test
cd ..

echo "Build complete!"
echo ""
echo "Deployment instructions:"
echo "========================"
echo "1. Commit all changes:"
echo "   git add ."
echo "   git commit -m 'Deploy to Heroku'"
echo ""
echo "2. Create Heroku app (if not created):"
echo "   heroku create your-app-name"
echo ""
echo "3. Add PostgreSQL addon:"
echo "   heroku addons:create heroku-postgresql:hobby-dev"
echo ""
echo "4. Set environment variables:"
echo "   heroku config:set SPRING_PROFILES_ACTIVE=heroku"
echo "   heroku config:set JWT_SECRET=$(openssl rand -base64 32)"
echo "   heroku config:set OPENAI_API_KEY=your-openai-api-key"
echo "   heroku config:set CORS_ALLOWED_ORIGINS=https://your-app-name.herokuapp.com"
echo ""
echo "5. Deploy:"
echo "   git push heroku main"
echo ""
echo "6. Check logs:"
echo "   heroku logs --tail"
