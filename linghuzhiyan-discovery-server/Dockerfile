# 发现服务 Dockerfile
# 注意：此 Dockerfile 假设 jar 包已经通过 mvn 命令构建完成
# 构建命令: mvn clean package -DskipTests
ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy

FROM ${BASE_IMAGE} AS runtime
LABEL org.opencontainers.image.title="linghuzhiyan-discovery-server" \
      org.opencontainers.image.description="Eureka Discovery Server" \
      org.opencontainers.image.source="https://example.local/linghuzhiyan"

WORKDIR /app

# 安装 curl（在切换用户之前）
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制已构建的 jar 包
COPY target/linghuzhiyan-discovery-server-1.0.0.jar /app/app.jar

# 创建非 root 用户并设置权限
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# 环境变量
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

EXPOSE 8761

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
