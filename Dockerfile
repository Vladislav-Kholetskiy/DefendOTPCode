# 1) Сборка внутри контейнера на базе JDK23 + Maven
FROM eclipse-temurin:23-jdk AS builder
WORKDIR /workspace

# Устанавливаем maven
RUN apt-get update \
 && apt-get install -y maven \
 && rm -rf /var/lib/apt/lists/*

# Копируем проект и собираем
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2) Финальный образ — только JRE и наш JAR
FROM eclipse-temurin:23-jre
WORKDIR /app

COPY --from=builder /workspace/target/DefendOTPCode-1.0.0.jar ./app.jar

EXPOSE 8080
CMD ["java", "--enable-preview", "-jar", "app.jar"]
