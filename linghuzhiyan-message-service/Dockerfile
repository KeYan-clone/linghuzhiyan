ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests -pl linghuzhiyan-message-service -am package

FROM ${BASE_IMAGE}
WORKDIR /app
COPY --from=build /workspace/linghuzhiyan-message-service/target/linghuzhiyan-message-service-1.0.0.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8085
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
