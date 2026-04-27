# Dockerfile para Backend (Spring Boot)
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/agrotech-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=docker"]
