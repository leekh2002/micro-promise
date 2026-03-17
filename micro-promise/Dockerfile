# 1단계: Gradle + JDK 17 환경에서 Spring Boot jar를 빌드한다.
FROM gradle:8.10.2-jdk17 AS builder

# 컨테이너 내부 작업 폴더를 /workspace로 지정한다.
WORKDIR /workspace

# Gradle 빌드에 필요한 설정 파일들을 먼저 복사한다.
# 소스보다 먼저 복사하면 Docker 레이어 캐시를 더 잘 활용할 수 있다.
COPY build.gradle settings.gradle gradlew gradlew.bat ./

# Gradle wrapper 디렉터리를 복사한다.
COPY gradle ./gradle

# 애플리케이션 소스 코드를 복사한다.
COPY src ./src

# 실행 가능한 Spring Boot jar를 생성한다.
RUN gradle bootJar --no-daemon

# 2단계: 실제 실행은 더 가벼운 JRE 이미지에서만 수행한다.
FROM eclipse-temurin:17-jre

# 실행용 작업 폴더를 /app으로 지정한다.
WORKDIR /app

# 빌드 단계에서 만든 jar를 가져와 app.jar라는 이름으로 둔다.
COPY --from=builder /workspace/build/libs/*.jar app.jar

# 애플리케이션이 컨테이너 내부에서 8080 포트를 사용함을 명시한다.
EXPOSE 8080

# 컨테이너 시작 시 Spring Boot jar를 실행한다.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
