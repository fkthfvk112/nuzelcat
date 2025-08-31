# Gradle 빌드 스테이지 (최신 Gradle 버전 사용)
FROM gradle:8.1-jdk17 AS builder

WORKDIR /app

# Gradle 빌드 파일과 소스 파일 복사
COPY build.gradle settings.gradle ./
COPY src/ src/

# Gradle Wrapper가 있다면 복사
COPY gradlew gradlew
COPY gradle/wrapper gradle/wrapper

# Gradle 빌드 실행 (Wrapper 사용, 테스트 테스크 제외)
RUN chmod +x gradlew
RUN ./gradlew build --no-daemon --stacktrace -x test

# 최종 실행 스테이지 (OpenJDK + 빌드된 JAR)
FROM openjdk:17-jdk-slim

# 시간대 설정
ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Seoul
RUN apt-get update && apt-get install -y tzdata && apt-get clean

WORKDIR /app

# Gradle 빌드 스테이지에서 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
