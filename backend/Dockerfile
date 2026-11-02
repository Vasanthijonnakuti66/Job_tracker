FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN mvn -B clean package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S jobtracker && adduser -S jobtracker -G jobtracker \
    && mkdir -p /app/uploads \
    && chown -R jobtracker:jobtracker /app

COPY --from=build --chown=jobtracker:jobtracker /workspace/target/job-tracker-*.jar /app/app.jar

USER jobtracker
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD wget -q -O /dev/null "http://127.0.0.1:${PORT:-${SERVER_PORT:-8080}}/api/health" || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
