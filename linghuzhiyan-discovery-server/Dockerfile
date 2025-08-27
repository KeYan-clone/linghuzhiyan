ARG BASE_IMAGE=eclipse-temurin:21-jre-jammy
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.title="linghuzhiyan-discovery-server" \
	org.opencontainers.image.description="Eureka Discovery Server" \
	org.opencontainers.image.source="https://example.local/linghuzhiyan"

WORKDIR /app

# Copy built jar from local target directory
ARG JAR_FILE=target/linghuzhiyan-discovery-server-1.0.0.jar
COPY ${JAR_FILE} /app/app.jar

# Allow tuning via JAVA_OPTS; Spring will bind to 0.0.0.0 by default here
ENV JAVA_OPTS=""

EXPOSE 8761

# Use shell form so JAVA_OPTS is expanded at runtime
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
