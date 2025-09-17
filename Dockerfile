# 1. JDK 기반 이미지 선택
FROM openjdk:17-jdk-slim

# 2. JAR 파일 복사
ARG JAR_FILE=build/libs/PlanMate-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 3. 앱 실행 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]