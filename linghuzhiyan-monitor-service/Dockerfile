ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy

# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# 1. 先只复制 pom 文件，下载依赖
COPY pom.xml .
COPY linghuzhiyan-monitor-service/pom.xml ./linghuzhiyan-monitor-service/
RUN mvn dependency:go-offline -B

# 2. 再复制源码并编译指定模块
COPY . .
RUN mvn -q -DskipTests -pl linghuzhiyan-monitor-service -am package

# Runtime stage
FROM ${BASE_IMAGE} AS runtime
LABEL org.opencontainers.image.title="linghuzhiyan-monitor-service" \
      org.opencontainers.image.description="Spring Boot Admin Server" \
      org.opencontainers.image.source="https://example.local/linghuzhiyan"

WORKDIR /app
COPY --from=build /workspace/linghuzhiyan-monitor-service/target/linghuzhiyan-monitor-service-1.0.0.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8090
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
