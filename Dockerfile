FROM openjdk:21-jdk-slim
WORKDIR /app

COPY build/libs/blood-spot-bot-api-0.0.1-SNAPSHOT.jar app.jar

LABEL authors="admin"

ENTRYPOINT ["java", "-jar", "app.jar"]