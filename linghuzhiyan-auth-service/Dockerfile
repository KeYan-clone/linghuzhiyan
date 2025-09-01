ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests -pl linghuzhiyan-auth-service -am package

FROM ${BASE_IMAGE}
LABEL org.opencontainers.image.title="linghuzhiyan-auth-service" \
      org.opencontainers.image.description="Auth Service" \
      org.opencontainers.image.source="https://example.local/linghuzhiyan"
WORKDIR /app
COPY --from=build /workspace/linghuzhiyan-auth-service/target/linghuzhiyan-auth-service-1.0.0.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8084
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
