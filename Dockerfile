# Build stage가 아닌 실행 환경으로 바로 설정 (CI에서 gradle 빌드 후 jar만 복사)
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR file from the build context
# Gradle 빌드 시 build/libs/디렉토리에 생성되는 jar 파일 복사
# -plain.jar 파일은 복사에서 제외하기 위해 빌드 파일명을 명시하거나 와일드카드 처리
COPY build/libs/*-SNAPSHOT.jar app.jar

# Spring Boot의 기본 포트 설정
EXPOSE 8080

# Profile 및 기타 JVM 옵션 설정 가능
ENV JAVA_OPTS=""

# Execute application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
