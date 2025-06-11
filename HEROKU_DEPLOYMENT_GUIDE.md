# Studia Heroku 배포 가이드

## 📋 사전 준비 사항

1. **Heroku CLI 설치**
   ```bash
   # Mac
   brew tap heroku/brew && brew install heroku
   
   # 로그인
   heroku login
   ```

2. **환경 변수 확인**
   - `OPENAI_API_KEY`: OpenAI API 키
   - `DATABASE_URL`: Heroku가 자동으로 설정 (PostgreSQL)
   - `JWT_SECRET`: JWT 토큰용 비밀 키
   - `CORS_ALLOWED_ORIGINS`: 프론트엔드 URL

## 🚀 배포 단계

### 1. 프로젝트 빌드
```bash
# 루트 디렉터리에서
./heroku-deploy.sh
```

### 2. Git 초기화 및 커밋
```bash
# Git 초기화 (필요한 경우)
git init

# .gitignore 확인
echo "*.log" >> .gitignore
echo ".env" >> .gitignore
echo ".DS_Store" >> .gitignore
echo "node_modules/" >> .gitignore
echo "build/" >> .gitignore
echo "target/" >> .gitignore

# 모든 파일 추가 및 커밋
git add .
git commit -m "Deploy Studia to Heroku"
```

### 3. Heroku 앱 생성 및 설정
```bash
# 앱 생성 (이미 생성된 경우 스킵)
heroku create studia-app-[your-unique-name]

# PostgreSQL 추가
heroku addons:create heroku-postgresql:hobby-dev

# 환경 변수 설정
heroku config:set SPRING_PROFILES_ACTIVE=heroku
heroku config:set JWT_SECRET=$(openssl rand -base64 32)
heroku config:set OPENAI_API_KEY=[your-openai-api-key]
heroku config:set CORS_ALLOWED_ORIGINS=https://[your-app-name].herokuapp.com
heroku config:set TZ=Asia/Seoul
```

### 4. 배포
```bash
# Heroku remote 추가 (이미 추가된 경우 스킵)
heroku git:remote -a [your-app-name]

# 배포
git push heroku main
```

### 5. 배포 확인
```bash
# 로그 확인
heroku logs --tail

# 앱 열기
heroku open

# 데이터베이스 마이그레이션 확인
heroku run echo "Database migrated"
```

## 🐛 문제 해결

### 1. Application Error 발생 시
```bash
# 상세 로그 확인
heroku logs --tail --app [your-app-name]

# 환경 변수 확인
heroku config

# 재시작
heroku restart
```

### 2. 데이터베이스 연결 실패
```bash
# DATABASE_URL 확인
heroku config:get DATABASE_URL

# PostgreSQL 정보 확인
heroku pg:info
```

### 3. 파일 업로드 실패
- Heroku의 파일 시스템은 임시적입니다
- 영구 저장이 필요한 경우 AWS S3 등 외부 스토리지 사용 권장

### 4. 메모리 부족
```bash
# 현재 dyno 정보 확인
heroku ps

# 필요시 dyno 타입 변경 (유료)
heroku ps:scale web=1:standard-1x
```

## ✅ 배포 후 확인 사항

1. **기본 기능 테스트**
   - [ ] 메인 페이지 접속
   - [ ] 회원가입
   - [ ] 로그인
   - [ ] 로그아웃

2. **핵심 기능 테스트**
   - [ ] Summary 파일 업로드
   - [ ] AI 요약 생성
   - [ ] Quiz 생성
   - [ ] Study Plan 일정 관리

3. **데이터베이스**
   - [ ] 사용자 정보 저장
   - [ ] 파일 메타데이터 저장
   - [ ] 일정 정보 저장

## 📱 모니터링

```bash
# 앱 상태 확인
heroku ps

# 메트릭 확인
heroku metrics

# 로그 스트림
heroku logs --tail

# 데이터베이스 백업
heroku pg:backups:capture
heroku pg:backups:schedule DATABASE_URL --at '02:00 Asia/Seoul'
```

## 🔒 보안 권장사항

1. **HTTPS 강제 적용** (Heroku는 기본 제공)
2. **환경 변수로 민감한 정보 관리**
3. **정기적인 백업 설정**
4. **로그 모니터링**

## 📞 지원

문제 발생 시:
1. Heroku 대시보드에서 로그 확인
2. `heroku logs --tail` 명령으로 실시간 로그 확인
3. Heroku Support 문의

---

**마지막 업데이트**: 2024-12-20
