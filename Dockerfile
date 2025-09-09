# 1. JDK 기반 이미지 선택
FROM openjdk:17-jdk

# 2. 작업 디렉토리 생성
WORKDIR /app

# 3. 소스 코드 복사
COPY . .

# 4. 필수 OS 패키지 설치
RUN apt-get update && apt-get install -y findutils

# 5. Gradle 빌드
RUN ./gradlew build -x test

# 6. 앱 실행
ENTRYPOINT ["java", "-jar", "build/libs/PlanMate-0.0.1-SNAPSHOT.jar"]