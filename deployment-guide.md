# Studia 프로젝트 AWS 배포 가이드

## 📋 사전 준비사항

1. AWS 계정
2. AWS CLI 설치 및 설정
3. Docker Desktop 설치
4. 도메인 (선택사항)

## 🚀 배포 단계

### 1. AWS 리소스 준비

#### 1.1 RDS (MySQL) 설정
```bash
# RDS MySQL 인스턴스 생성
aws rds create-db-instance \
  --db-instance-identifier studia-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0 \
  --master-username admin \
  --master-user-password [YOUR_PASSWORD] \
  --allocated-storage 20 \
  --vpc-security-group-ids [YOUR_SECURITY_GROUP]
```

#### 1.2 S3 버킷 생성 (파일 업로드용)
```bash
aws s3 mb s3://studia-uploads
aws s3api put-bucket-cors --bucket studia-uploads --cors-configuration file://cors.json
```

### 2. 백엔드 배포 (Elastic Beanstalk)

#### 2.1 Dockerfile 수정
```dockerfile
FROM openjdk:17-alpine
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","/app.jar"]
```

#### 2.2 application-prod.yml 생성
```yaml
spring:
  datasource:
    url: jdbc:mysql://${RDS_HOSTNAME}:${RDS_PORT}/${RDS_DB_NAME}
    username: ${RDS_USERNAME}
    password: ${RDS_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

openai:
  api:
    key: ${OPENAI_API_KEY}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

aws:
  s3:
    bucket: studia-uploads
    region: ap-northeast-2
```

#### 2.3 Elastic Beanstalk 배포
```bash
# EB CLI 초기화
eb init -p docker studia-backend

# 환경 생성
eb create studia-backend-env

# 환경 변수 설정
eb setenv RDS_HOSTNAME=your-rds-endpoint \
  RDS_PORT=3306 \
  RDS_DB_NAME=studia \
  RDS_USERNAME=admin \
  RDS_PASSWORD=your-password \
  OPENAI_API_KEY=your-openai-key \
  JWT_SECRET=your-jwt-secret

# 배포
eb deploy
```

### 3. 프론트엔드 배포 (S3 + CloudFront)

#### 3.1 API 엔드포인트 업데이트
```javascript
// Frontend/assets/js/api.js
const API_BASE_URL = 'https://api.studia.com'; // EB 엔드포인트로 변경
```

#### 3.2 S3 정적 웹사이트 호스팅
```bash
# S3 버킷 생성
aws s3 mb s3://studia-frontend

# 웹사이트 호스팅 활성화
aws s3 website s3://studia-frontend \
  --index-document index.html \
  --error-document error.html

# 파일 업로드
aws s3 sync ./Frontend s3://studia-frontend --acl public-read
```

#### 3.3 CloudFront 배포
```bash
# CloudFront 배포 생성
aws cloudfront create-distribution \
  --origin-domain-name studia-frontend.s3-website.ap-northeast-2.amazonaws.com \
  --default-root-object index.html
```

### 4. 도메인 연결 (Route 53)

```bash
# 호스팅 존 생성
aws route53 create-hosted-zone --name studia.com

# A 레코드 생성 (CloudFront)
# Route 53 콘솔에서 설정 권장
```

### 5. HTTPS 설정

#### 5.1 ACM 인증서 발급
```bash
aws acm request-certificate \
  --domain-name studia.com \
  --validation-method DNS
```

#### 5.2 CloudFront에 인증서 적용
- CloudFront 콘솔에서 설정

### 6. CI/CD 파이프라인 (GitHub Actions)

#### 6.1 백엔드 배포 워크플로우
```yaml
name: Deploy Backend

on:
  push:
    branches: [ main ]
    paths:
      - 'Backend/**'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
          
      - name: Build with Gradle
        working-directory: ./Backend
        run: ./gradlew build
        
      - name: Deploy to EB
        uses: einaregilsson/beanstalk-deploy@v20
        with:
          aws_access_key: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws_secret_key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          application_name: studia-backend
          environment_name: studia-backend-env
          version_label: ${{ github.sha }}
          region: ap-northeast-2
          deployment_package: Backend/build/libs/*.jar
```

#### 6.2 프론트엔드 배포 워크플로우
```yaml
name: Deploy Frontend

on:
  push:
    branches: [ main ]
    paths:
      - 'Frontend/**'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v1
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ap-northeast-2
          
      - name: Deploy to S3
        run: |
          aws s3 sync ./Frontend s3://studia-frontend --delete --acl public-read
          
      - name: Invalidate CloudFront
        run: |
          aws cloudfront create-invalidation \
            --distribution-id ${{ secrets.CLOUDFRONT_DISTRIBUTION_ID }} \
            --paths "/*"
```

### 7. 모니터링 설정

#### 7.1 CloudWatch 알람
```bash
# RDS CPU 사용률 알람
aws cloudwatch put-metric-alarm \
  --alarm-name studia-rds-cpu \
  --alarm-description "RDS CPU usage" \
  --metric-name CPUUtilization \
  --namespace AWS/RDS \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold
```

#### 7.2 로그 수집
- CloudWatch Logs에 애플리케이션 로그 전송
- X-Ray를 통한 분산 추적

### 8. 보안 설정

#### 8.1 보안 그룹
- RDS: 백엔드 서버에서만 접근 가능
- 백엔드: 80/443 포트만 개방
- CloudFront를 통해서만 프론트엔드 접근

#### 8.2 환경 변수 암호화
- AWS Systems Manager Parameter Store 사용
- 민감한 정보는 SecureString으로 저장

### 9. 비용 최적화

#### 9.1 Auto Scaling
```bash
# Auto Scaling 그룹 생성
eb scale 2 --timeout 5
```

#### 9.2 예약 인스턴스
- 장기 사용 시 Reserved Instances 구매

### 10. 백업 및 복구

#### 10.1 RDS 자동 백업
```bash
# 백업 보존 기간 설정 (7일)
aws rds modify-db-instance \
  --db-instance-identifier studia-db \
  --backup-retention-period 7
```

#### 10.2 S3 버전 관리
```bash
aws s3api put-bucket-versioning \
  --bucket studia-uploads \
  --versioning-configuration Status=Enabled
```

## 📊 예상 비용 (월간)

- EC2 (t3.micro): $8.50
- RDS (db.t3.micro): $15.00
- S3 + CloudFront: $5.00
- Route 53: $0.50
- **총계: 약 $29/월**

## 🔧 문제 해결

### 일반적인 문제

1. **CORS 오류**
   - CloudFront 동작에 CORS 헤더 추가
   - 백엔드 SecurityConfig 확인

2. **RDS 연결 실패**
   - 보안 그룹 규칙 확인
   - RDS 퍼블릭 액세스 설정

3. **파일 업로드 실패**
   - S3 버킷 정책 확인
   - IAM 역할 권한 확인

### 로그 확인
```bash
# EB 로그 확인
eb logs

# CloudWatch 로그 확인
aws logs tail /aws/elasticbeanstalk/studia-backend-env/var/log/eb-docker/containers/eb-current-app
```

## 📱 추가 개선사항

1. **PWA 구현**
   - Service Worker 추가
   - 오프라인 지원

2. **CDN 최적화**
   - 이미지 최적화
   - JS/CSS 압축

3. **데이터베이스 최적화**
   - 인덱스 추가
   - 쿼리 최적화

4. **멀티 리전 지원**
   - CloudFront 글로벌 배포
   - RDS 읽기 전용 복제본

---

배포에 대한 질문이 있으시면 sooheechoi@sju.ac.kr로 문의해주세요.
