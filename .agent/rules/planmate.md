---
trigger: always_on
---

# PlanMate 프로젝트 커스텀 룰 설정
project_name: "PlanMate"
principles:
  - "모든 코드는 기존에 작성된 유틸리티와 공통 모듈을 최우선으로 재사용한다."
  - "프로젝트 특유의 레이어드 아키텍처와 패키지 구조를 엄격히 준수한다."

rules:
  # 1. 아키텍처 및 폴더 구조 준수
  - id: "enforce_project_structure"
    instruction: |
      새로운 기능 구현 시 다음의 구조를 반드시 지켜줘:
      - Backend: Java 17 / Spring Boot 기반. 
        계층 구조: [Controller] -> [Service] -> [Repository] -> [Entity/DTO].
        패키지: 비즈니스 로직은 `com.example.planmate.domain.[도메인명]`에 위치.
      - Frontend: React / Vite / Zustand 기반.
        계층 구조: [Page] -> [Component] -> [Hooks/Store].
        폴더: 공통 컴포넌트는 `src/components/common`, 상태 관리는 `src/store` 활용.

  # 2. 코드 재사용 (DRY 원칙 강화)
  - id: "maximal_code_reuse"
    instruction: |
      중복 코드를 지양하고 다음 모듈을 먼저 활용해:
      - 실시간 동기화/캐싱: `sharedsync` 모듈의 `@CacheEntity`, `@PresenceUser` 활용. 직접 구현 금지.
      - 외부 연동: `common/externalAPI`의 Google Map, Weather 서비스 재사용.
      - 프론트엔드: API 요청은 `useApiClient` 커스텀 훅을 사용하고, 중복 UI는 `common/` 컴포넌트 활용.
      - 새로운 로직 작성 전, 반드시 프로젝트 내 유사한 기능이 있는지 먼저 검색해.

  # 3. 도메인 및 데이터 컨벤션
  - id: "naming_and_dto_convention"
    instruction: |
      - API 응답은 항상 `CommonResponse` 객체로 감싸서 반환해.
      - 예외 처리는 `GlobalExceptionHandler`와 기존 Custom Exception 클래스들을 상속받아 사용해.
      - DB 관련 변경은 `src/main/resources/db/migration`의 Flyway 스크립트 형식을 참고해.

  # 4. 인프라 및 배포 맥락
  - id: "infrastructure_context"
    instruction: |
      - 배포 환경은 Kubernetes(k8s)와 Docker 기반임을 인지해.
      - 환경 설정은 `application.yml`을 참조하고, 인프라 변경 제안 시 `k8s/` 폴더의 YAML 파일 구조를 따져봐.