# Простой Dockerfile - использует готовый JAR

FROM amazoncorretto:17

# Установить curl для health checks
RUN yum install -y curl && yum clean all

WORKDIR /app

# Копируем уже собранный JAR
COPY build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
    CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
