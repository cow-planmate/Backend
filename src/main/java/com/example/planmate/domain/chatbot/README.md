# 스마트 챗봇 API 가이드 🤖

## 개요
Google Gemini API를 활용한 **지능형 여행 계획 도우미 챗봇**입니다.
단순한 대화뿐만 아니라 **실제 여행 계획을 수정**하고 **WebSocket을 통해 실시간으로 동기화**합니다!

## ✨ 주요 기능

### 🎯 AI 함수 호출 (Function Calling)
챗봇이 자연어를 이해하고 실제 여행 계획을 수정할 수 있습니다:

#### 📋 **Plan 관리** (update만 지원)
- **계획 이름 변경**: "계획 이름을 '부산 여행'으로 바꿔줘"
- **출발지 변경**: "출발지를 서울역으로 변경해줘"
- **인원 수 조정**: "성인 2명, 어린이 1명으로 바꿔줘"
- **교통수단 변경**: "교통수단을 대중교통으로 바꿔줘"

#### 📅 **TimeTable 관리** (create, update, delete 지원)
- **일정 생성**: "8월 15일 새로운 일정을 만들어줘"
- **일정 수정**: "첫 번째 일정을 8월 16일로 바꿔줘"
- **일정 삭제**: "마지막 일정을 삭제해줘"

#### 📍 **Place Block 관리** (create, update, delete 지원)
- **장소 추가**: "경복궁을 오전 10시에 추가해줘"
- **장소 수정**: "경복궁 시간을 오후 2시로 바꿔줘"
- **장소 삭제**: "경복궁을 일정에서 제거해줘"

### 🔄 실시간 동기화
- **Redis** 데이터 실시간 업데이트
- **WebSocket** 브로드캐스트로 모든 클라이언트 즉시 반영
- 다중 사용자 협업 지원

## 설정 방법

### 1. Google Gemini API 키 발급
1. [Google AI Studio](https://makersuite.google.com/app/apikey)에 접속
2. API 키 생성
3. 환경변수 또는 application.yml에 설정

### 2. 환경변수 설정
```bash
export GOOGLE_GEMINI_API_KEY=your_actual_api_key_here
```

## API 사용법

### 📱 기본 채팅
**Endpoint:** `POST /api/chatbot/chat`

**Request Body:**
```json
{
  "message": "안녕하세요!",
  "userId": "user123",
  "planId": null
}
```

### 🛠️ 계획 수정 채팅
**Endpoint:** `POST /api/chatbot/chat`

**Request Body:**
```json
{
  "message": "계획 이름을 '제주도 힐링 여행'으로 바꿔줘",
  "userId": "user123",
  "planId": 12345
}
```

**Response:**
```json
{
  "response": "계획 이름을 '제주도 힐링 여행'으로 변경하겠습니다.\n\n✅ 계획 이름을 '제주도 힐링 여행'으로 변경했습니다.",
  "timestamp": "2025-10-07T14:52:59.123",
  "success": true,
  "errorMessage": null
}
```

## 🎮 사용 예시

### 계획 이름 변경
```
사용자: "여행 제목을 '가족과 함께하는 부산 여행'으로 바꿔줘"
챗봇: "계획 이름을 '가족과 함께하는 부산 여행'으로 변경하겠습니다.
      ✅ 계획 이름을 '가족과 함께하는 부산 여행'으로 변경했습니다."
```

### 출발지 변경
```
사용자: "출발지를 인천공항으로 바꿔줘"
챗봇: "출발지를 인천공항으로 변경하겠습니다.
      ✅ 출발지를 '인천공항'으로 변경했습니다."
```

### 인원 수 변경
```
사용자: "성인 4명, 어린이 2명으로 바꿔줘"
챗봇: "인원 수를 변경하겠습니다.
      ✅ 인원 수를 변경했습니다. 성인: 4명 어린이: 2명"
```

### 교통수단 변경
```
사용자: "이동 수단을 자동차로 바꿔줘"
챗봇: "교통수단을 자동차로 변경하겠습니다.
      ✅ 교통수단을 '자동차'로 변경했습니다."
```

## 🏗️ 구현 구조
```
src/main/java/com/example/planmate/domain/chatbot/
├── controller/
│   └── ChatBotController.java          # REST API + WebSocket 브로드캐스트
├── dto/
│   ├── ChatBotRequest.java             # 요청 DTO (planId 포함)
│   ├── ChatBotResponse.java            # 응답 DTO
│   └── ChatBotActionResponse.java      # 액션 응답 DTO
└── service/
    ├── ChatBotService.java             # AI 대화 + 함수 호출 처리
    └── ChatBotPlanService.java         # 모든 CRUD 함수들
```

## 🔄 WebSocket 액션 처리

### 지원되는 액션 타입:
```json
{
  "action": "create|update|delete",
  "targetName": "plan|timeTable|timeTablePlaceBlock",
  "target": { /* 해당 DTO 객체 */ }
}
```

### WebSocket 토픽:
- **Plan**: `/topic/plan/{planId}/update/plan`
- **TimeTable**: 
  - `/topic/plan/{planId}/create/timetable`
  - `/topic/plan/{planId}/update/timetable`
  - `/topic/plan/{planId}/delete/timetable`
- **PlaceBlock**:
  - `/topic/plan/{planId}/create/timetableplaceblock`
  - `/topic/plan/{planId}/update/timetableplaceblock`
  - `/topic/plan/{planId}/delete/timetableplaceblock`

## 🎮 액션별 사용 예시

### Plan 수정 (update만 가능)
```
사용자: "여행 제목을 '가족과 함께하는 부산 여행'으로 바꿔줘"
챗봇: "여행 계획 이름을 '가족과 함께하는 부산 여행'으로 변경했습니다! ✅"
액션: { action: "update", targetName: "plan", target: WPlanRequest }
```

### TimeTable 관리 (create, update, delete)
```
사용자: "8월 15일 새로운 일정을 만들어줘"
챗봇: "2024-08-15 날짜의 새로운 일정을 생성했습니다! 📅"
액션: { action: "create", targetName: "timeTable", target: WTimetableRequest }
```

### PlaceBlock 관리 (create, update, delete)
```
사용자: "경복궁을 오전 10시에 추가해줘"
챗봇: "'경복궁' 장소를 일정에 추가했습니다! 📍"
액션: { action: "create", targetName: "timeTablePlaceBlock", target: WTimeTablePlaceBlockRequest }
```

## 🔧 기술 스택
- **AI**: Google Gemini 2.0 Flash
- **프롬프트 엔지니어링**: 함수 호출 명령어 파싱
- **실시간 통신**: WebSocket (SimpMessagingTemplate)
- **데이터 저장**: Redis (WebSocketPlanService)
- **패턴 매칭**: Java Regex

## ⚠️ 주의사항
- Google Gemini API 키가 필요합니다
- API 사용량에 따라 요금이 부과될 수 있습니다
- planId가 있어야 계획 수정 기능이 동작합니다
- WebSocket 연결이 필요합니다 (실시간 동기화)

## 🚀 향후 확장 가능성
- 장소 추가/삭제 기능
- 일정 시간 변경
- 예산 관리
- 추천 시스템 연동
- 다국어 지원

이제 사용자들이 자연어로 여행 계획을 실시간으로 수정할 수 있습니다! 🌟