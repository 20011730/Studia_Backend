# Studia Backend - MySQL 설정 가이드

## 📊 대화 길이 추적
```
[■■■■□□□□□□□□□□□□□□□□] 20%
```

## 데이터베이스 설정

### 1. MySQL 설치 및 설정

#### Mac (Homebrew)
```bash
brew install mysql
brew services start mysql
```

#### Windows
MySQL 공식 웹사이트에서 MySQL Community Server를 다운로드하여 설치

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
```

### 2. 데이터베이스 및 사용자 생성

MySQL에 접속:
```bash
mysql -u root -p
```

다음 SQL 명령어 실행:
```sql
-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS studia_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER IF NOT EXISTS 'studia_user'@'localhost' IDENTIFIED BY 'studia_password';
GRANT ALL PRIVILEGES ON studia_db.* TO 'studia_user'@'localhost';
FLUSH PRIVILEGES;

-- 확인
SHOW DATABASES;
USE studia_db;
```

### 3. 애플리케이션 실행

#### 개발 환경 (H2 Database)
```bash
./gradlew bootRun
```

#### 프로덕션 환경 (MySQL)
```bash
# 환경 변수 설정
export SPRING_PROFILES_ACTIVE=prod
export DB_USERNAME=studia_user
export DB_PASSWORD=studia_password

# 실행
./gradlew bootRun
```

또는 IntelliJ에서 실행 시:
- Run Configuration에서 Environment variables 추가:
  - `SPRING_PROFILES_ACTIVE=prod`
  - `DB_USERNAME=studia_user`
  - `DB_PASSWORD=studia_password`

### 4. 데이터베이스 연결 확인

애플리케이션 실행 후 로그에서 다음과 같은 메시지 확인:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

### 5. 테이블 자동 생성

`spring.jpa.hibernate.ddl-auto=update` 설정으로 인해 애플리케이션 실행 시 자동으로 테이블이 생성됩니다.

생성된 테이블 확인:
```sql
USE studia_db;
SHOW TABLES;
```

### 6. 문제 해결

#### Connection refused 에러
- MySQL 서비스가 실행 중인지 확인
- 포트 3306이 열려 있는지 확인

#### Access denied 에러
- 사용자명과 비밀번호 확인
- 사용자 권한 확인

#### Unknown database 에러
- 데이터베이스가 생성되었는지 확인
- 데이터베이스 이름 확인

## 프로파일 설정

- `dev` (기본값): H2 인메모리 데이터베이스 사용
- `prod`: MySQL 데이터베이스 사용

## 환경 변수

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| SPRING_PROFILES_ACTIVE | dev | 실행 프로파일 |
| DB_USERNAME | studia_user | MySQL 사용자명 |
| DB_PASSWORD | studia_password | MySQL 비밀번호 |
| JWT_SECRET | (생성된 값) | JWT 토큰 시크릿 |
| OPENAI_API_KEY | test-key | OpenAI API 키 |
| CLAUDE_API_KEY | test-key | Claude API 키 |
