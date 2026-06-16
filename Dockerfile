FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN useradd --system --uid 10001 appuser

COPY --from=build /workspace/target/fee-management-1.0.0.jar app.jar

ENV SERVER_PORT=8082

EXPOSE 8082

USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
