# 챗봇 API 가이드

## 개요
Google Gemini API를 활용한 간단한 챗봇 서비스입니다.

## 설정 방법

### 1. Google Gemini API 키 발급
1. [Google AI Studio](https://makersuite.google.com/app/apikey)에 접속
2. API 키 생성
3. 환경변수 또는 application.yml에 설정

### 2. 환경변수 설정
```bash
export GOOGLE_GEMINI_API_KEY=your_actual_api_key_here
```

또는 application.yml에서 직접 설정:
```yaml
google:
  gemini:
    api:
      key: your_actual_api_key_here
```

## API 사용법

### 챗봇과 대화하기
**Endpoint:** `POST /api/chatbot/chat`

**Request Body:**
```json
{
  "message": "안녕하세요!",
  "userId": "user123"
}
```

**Response:**
```json
{
  "response": "안녕하세요! 무엇을 도와드릴까요?",
  "timestamp": "2025-10-06T20:43:46.123",
  "success": true,
  "errorMessage": null
}
```

### 서비스 상태 확인
**Endpoint:** `GET /api/chatbot/health`

**Response:**
```
ChatBot service is running!
```

## 테스트 예시

### cURL을 사용한 테스트
```bash
curl -X POST http://localhost:8080/api/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "오늘 날씨가 어때?",
    "userId": "test_user"
  }'
```

### Postman을 사용한 테스트
1. POST 요청으로 `http://localhost:8080/api/chatbot/chat` 설정
2. Body에 JSON 형태로 메시지 전송
3. 응답 확인

## 주의사항
- Google Gemini API 키가 필요합니다
- API 사용량에 따라 요금이 부과될 수 있습니다
- 네트워크 연결이 필요합니다

## 구현 구조
```
src/main/java/com/example/planmate/domain/chatbot/
├── controller/
│   └── ChatBotController.java      # REST API 엔드포인트
├── dto/
│   ├── ChatBotRequest.java         # 요청 DTO
│   └── ChatBotResponse.java        # 응답 DTO
└── service/
    └── ChatBotService.java         # Google Gemini API 호출 서비스
```