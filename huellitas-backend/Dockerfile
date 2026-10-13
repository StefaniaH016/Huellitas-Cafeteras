# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY huellitas-backend/pom.xml .
COPY huellitas-backend/.mvn .mvn
COPY huellitas-backend/mvnw .

RUN mvn dependency:resolve

COPY huellitas-backend/src src

RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/huellitas-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
