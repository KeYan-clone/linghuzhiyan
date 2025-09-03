ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# 1. 先只复制 pom 文件，下载依赖
COPY pom.xml .
COPY linghuzhiyan-discovery-server/pom.xml ./linghuzhiyan-discovery-server/
RUN mvn -f linghuzhiyan-discovery-server/pom.xml dependency:go-offline -B

# 2. 再复制源码并编译指定模块
COPY . .
RUN mvn -q -DskipTests -pl linghuzhiyan-discovery-server -am package

FROM ${BASE_IMAGE} AS runtime
LABEL org.opencontainers.image.title="linghuzhiyan-discovery-server" \
	or.opencontainers.image.description="Eureka Discovery Server" \
	or.opencontainers.image.source="https://example.local/linghuzhiyan"
WORKDIR /app
COPY --from=build /workspace/linghuzhiyan-discovery-server/target/linghuzhiyan-discovery-server-1.0.0.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8761
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
