# 베이스 이미지 (JDK 17)
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /onharu

# 애플리케이션 JAR 파일을 컨테이너에 복사
ARG JAR_FILE=build/libs/onharu-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} ./onharu.jar

# 애플리케이션 포트
EXPOSE 8080

# 실행 명령
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/onharu/onharu.jar"]
