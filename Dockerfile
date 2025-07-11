FROM maven:3.9.8-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B


FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup
COPY --from=build --chown=appuser:appgroup /app/target/*.war app.war
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
