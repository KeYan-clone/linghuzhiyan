ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.title="linghuzhiyan-monitor-service" \
      org.opencontainers.image.description="Spring Boot Admin Server" \
      org.opencontainers.image.source="https://example.local/linghuzhiyan"

WORKDIR /app

ARG JAR_FILE=target/linghuzhiyan-monitor-service-1.0.0.jar
COPY ${JAR_FILE} /app/app.jar

ENV JAVA_OPTS=""

EXPOSE 8090

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
