# 1단계: 빌드 스테이지
FROM gradle:7.6-jdk17 AS builder

WORKDIR /app

COPY . .

# 컨테이너 안에서 직접 빌드 실행 (서버에 Java 없어도 됨)
RUN ./gradlew clean build -x test

# 2단계: 실행 스테이지 (경량화)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /onharu

# 1단계에서 만든 JAR만 쏙 빼오기
COPY --from=builder /app/build/libs/onharu-0.0.1-SNAPSHOT.jar ./onharu.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "onharu.jar"]