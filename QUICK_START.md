# Studia 빠른 시작 가이드

## 🚀 프로젝트 실행 방법

### 1. 백엔드 서버 실행
터미널을 열고 다음 명령어를 실행하세요:

```bash
cd Backend
./run.sh
```

백엔드 서버가 http://localhost:8080 에서 시작됩니다.

### 2. 프론트엔드 서버 실행
새로운 터미널을 열고 다음 명령어를 실행하세요:

```bash
cd Frontend
./run.sh
```

프론트엔드 서버가 http://localhost:3000 에서 시작됩니다.

### 3. 웹 브라우저에서 접속
브라우저를 열고 http://localhost:3000 으로 접속하세요.

## ⚠️ 주의사항

1. **MySQL이 실행 중이어야 합니다**
   - 데이터베이스: studia_db
   - 사용자: studia_user
   - 비밀번호: studia_password

2. **환경 변수 설정 필요**
   - Backend/.env 파일에 OpenAI API 키가 설정되어 있어야 합니다

3. **백엔드 서버가 실행되지 않으면**
   - 메모리 부족일 수 있습니다
   - Backend/run.sh 파일의 JAVA_OPTS 값을 조정하세요

## 🔧 문제 해결

### 백엔드 서버 오류
```bash
# 로그 확인
tail -f Backend/app.log
```

### 프론트엔드 접속 불가
- 포트 3000이 다른 프로세스에서 사용 중인지 확인
```bash
lsof -i :3000
```

## 📋 테스트 계정
기본 테스트 계정으로 로그인하려면:
1. Sign Up 버튼 클릭
2. 정보 입력 후 회원가입
3. Log In으로 로그인

## 🎯 주요 기능 테스트

1. **Summary (요약)**: 파일 업로드 → AI 요약 생성
2. **Quiz (퀴즈)**: 요약된 자료에서 퀴즈 생성
3. **Study Plan (학습 계획)**: 캘린더에 일정 추가
4. **Dashboard**: 학습 통계 확인
5. **Friends**: 친구 추가 및 스터디 그룹 생성
