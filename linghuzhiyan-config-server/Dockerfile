ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# 1. 先只复制 pom 文件，下载依赖
COPY pom.xml .
COPY linghuzhiyan-config-server/pom.xml ./linghuzhiyan-config-server/
RUN mvn dependency:go-offline -B

# 2. 再复制源码并编译指定模块
COPY . .
RUN mvn -q -DskipTests -pl linghuzhiyan-config-server -am package

FROM ${BASE_IMAGE} AS runtime
LABEL org.opencontainers.image.title="linghuzhiyan-config-server" \
      org.opencontainers.image.description="Spring Cloud Config Server" \
      org.opencontainers.image.source="https://example.local/linghuzhiyan"
WORKDIR /app
# 安装 curl，用于容器健康检查
RUN apt-get update \
      && apt-get install -y --no-install-recommends curl \
      && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/linghuzhiyan-config-server/target/linghuzhiyan-config-server-1.0.0.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8888
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
