# 베이스 이미지 선택
FROM openjdk:21

# JAR 파일 복사
ARG JAR_FILE=build/libs/my-back-end-app-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 포트 설정
EXPOSE 7400

# 애플리케이션 실행
ENTRYPOINT ["java","-jar","/app.jar"]