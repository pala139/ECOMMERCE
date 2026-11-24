FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn/docker-settings.xml /tmp/maven-settings.xml
COPY src src
# Shared build runners can be rate-limited by Maven Central.
RUN mvn -B -U -s /tmp/maven-settings.xml -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/ecommerce-backend-1.0.0.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
