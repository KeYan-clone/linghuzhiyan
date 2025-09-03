ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# 1. 先只复制 pom 文件，下载依赖
COPY pom.xml .
COPY linghuzhiyan-auth-service/pom.xml ./linghuzhiyan-auth-service/
RUN mvn -f linghuzhiyan-auth-service/pom.xml dependency:go-offline -B

# 2. 再复制源码并编译指定模块
COPY . .
RUN mvn -q -DskipTests -pl linghuzhiyan-auth-service -am package

FROM ${BASE_IMAGE} AS runtime
LABEL org.opencontainers.image.title="linghuzhiyan-auth-service" \
      org.opencontainers.image.description="Auth Service" \
      org.opencontainers.image.source="https://example.local/linghuzhiyan"
WORKDIR /app
COPY --from=build /workspace/linghuzhiyan-auth-service/target/linghuzhiyan-auth-service-1.0.0.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8084
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
