# 1. JDK 기반 이미지 선택
FROM openjdk:17-jdk

# 2. 작업 디렉토리 생성
WORKDIR /app

# 3. 소스 코드 복사
COPY . .

# 4. Gradle 빌드
RUN ./gradlew build -x test

# 5. JAR 복사
ARG JAR_FILE=build/libs/PlanMate-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 6. 앱 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
