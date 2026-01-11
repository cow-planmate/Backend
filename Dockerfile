# 1. JDK 기반 이미지
FROM eclipse-temurin:17-jdk-jammy

# 2. Spring Boot JAR 복사 (버전에 상관없이 jar 하나만 가져오기)
ARG JAR_FILE=build/libs/PlanMate-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app.jar

# 4. 앱 실행 (JDK 17 + 한국 시간 설정)
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]


# # 1. JDK 기반 이미지
# FROM eclipse-temurin:17-jdk-jammy

# # 2. APM 에이전트 추가
# COPY elastic-apm-agent.jar /elastic-apm-agent.jar

# # 3. Spring Boot JAR 복사 (버전에 상관없이 jar 하나만 가져오기)
# ARG JAR_FILE=build/libs/PlanMate-0.0.1-SNAPSHOT.jar
# COPY ${JAR_FILE} /app.jar

# # 4. 앱 실행
# ENTRYPOINT ["java", "-javaagent:/elastic-apm-agent.jar", "-Delastic.apm.service_name=planmate-app", "-Delastic.apm.server_urls=http://apm-server:8200", "-Delastic.apm.application_packages=com.example.planmate", "-Delastic.apm.environment=docker", "-jar", "/app.jar"]
