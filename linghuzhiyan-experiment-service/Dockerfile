ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy

# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
# 仅构建该模块及依赖，跳过测试，确保产物生成
RUN mvn -q -DskipTests -DskipITs -pl linghuzhiyan-experiment-service -am clean package

# Runtime stage
FROM ${BASE_IMAGE} AS runtime
WORKDIR /app
# 使用通配符复制 Spring Boot 可执行 Jar，避免固定版本号
COPY --from=build /workspace/linghuzhiyan-experiment-service/target/linghuzhiyan-experiment-service-*.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8083
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
