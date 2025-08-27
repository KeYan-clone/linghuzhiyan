# 基于 OpenJDK 21 的 Spring Boot 镜像（与编译版本一致）
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 应用 jar 包名
ARG JAR_FILE=target/linghuzhiyan-user-service-1.0.0.jar

# 拷贝 jar 包到容器
COPY ${JAR_FILE} /app/app.jar

# 允许通过环境变量追加 JVM 参数
ENV JAVA_OPTS=""

# 暴露端口（根据实际服务端口修改）
EXPOSE 8080

# 使用 shell 形式以展开 JAVA_OPTS
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
