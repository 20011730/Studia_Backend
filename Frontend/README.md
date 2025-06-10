# Studia Frontend (HTML5/CSS/JS)

간단한 HTML5/CSS/JavaScript로 구현된 Studia 프론트엔드입니다.

## 시작하기

1. 백엔드 서버가 실행 중인지 확인하세요 (http://localhost:8080)

2. 프론트엔드 서버 시작:
```bash
cd Frontend
python3 server.py
```

3. 브라우저에서 http://localhost:3000 접속

## 기능

- **회원가입/로그인**: JWT 기반 인증
- **학습 자료 업로드**: PDF, DOCX, PPTX, TXT 파일 지원
- **자동 요약 및 핵심 포인트 추출**
- **퀴즈 생성**: 5-50개 사이의 퀴즈 자동 생성
- **퀴즈 풀기**: 문제별 힌트 및 설명 제공
- **오답노트**: 틀린 문제 자동 저장 및 복습
- **대시보드**: 학습 통계 확인

## 테스트 계정

아직 회원가입을 하지 않았다면 새로 만들어주세요:
- 이메일: test@example.com
- 비밀번호: password123

## 주의사항

- 이는 테스트용 간단한 구현입니다
- 실제 프로덕션에서는 React/Vue/Angular 등의 프레임워크 사용 권장
- 보안을 위해 HTTPS 사용 필요
- 에러 처리 및 로딩 상태 관리 개선 필요
