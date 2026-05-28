FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY dataset-service/pom.xml dataset-service/pom.xml
COPY analytics-service/pom.xml analytics-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY analytics-service/src analytics-service/src

RUN mvn -pl analytics-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/analytics-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
