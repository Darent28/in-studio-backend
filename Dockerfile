# -- Build stage --
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy everything needed for the build
COPY .mvn .mvn
COPY mvnw pom.xml settings.xml ./
COPY src ./src

# Build the jar in a single step (skip tests in image build)
RUN chmod +x mvnw && touch .env && ./mvnw clean package -DskipTests -B

# -- Runtime stage --
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE ${SERVER_PORT:-8080}

ENTRYPOINT ["java", "-jar", "app.jar"]
