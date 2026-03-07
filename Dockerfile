# Используем Gradle + JDK образ
FROM gradle:8.5.0-jdk21 AS builder

WORKDIR /app

# Копируем весь проект
COPY . .

# Собираем jar (без тестов для скорости)
RUN gradle bootJar -x test

# ========================================
# Финальный образ
FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

# Копируем jar из предыдущего этапа
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
