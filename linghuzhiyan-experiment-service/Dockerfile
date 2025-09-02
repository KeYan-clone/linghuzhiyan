ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# 1. 先只复制 pom 文件，下载依赖
COPY pom.xml .
COPY linghuzhiyan-experiment-service/pom.xml ./linghuzhiyan-experiment-service/
RUN mvn dependency:go-offline -B

# 2. 再复制源码并编译指定模块
COPY . .
RUN mvn -q -DskipTests -pl linghuzhiyan-experiment-service -am package

FROM ${BASE_IMAGE} AS runtime
WORKDIR /app
COPY --from=build /workspace/linghuzhiyan-experiment-service/target/linghuzhiyan-experiment-service-1.0.0.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8083
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
