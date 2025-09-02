ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy

# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# 1. 先只复制 pom 文件，下载依赖
COPY pom.xml .
COPY linghuzhiyan-gateway/pom.xml ./linghuzhiyan-gateway/
RUN mvn dependency:go-offline -B

# 2. 再复制源码并编译指定模块
COPY . .
RUN mvn -q -DskipTests -pl linghuzhiyan-gateway -am package

# Runtime stage
FROM ${BASE_IMAGE} AS runtime
LABEL org.opencontainers.image.title="linghuzhiyan-gateway" \
      org.opencontainers.image.description="Spring Cloud Gateway" \
      org.opencontainers.image.source="https://example.local/linghuzhiyan"
WORKDIR /app
COPY --from=build /workspace/linghuzhiyan-gateway/target/linghuzhiyan-gateway-1.0.0.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
