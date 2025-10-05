FROM openjdk:17-jdk-slim

WORKDIR /app

# 创建日志目录
RUN mkdir -p /app/logs && chmod 755 /app/logs

COPY server-web/target/server-web-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

# 添加启动参数，确保日志输出到控制台和文件
ENTRYPOINT ["java", "-jar", "app.jar", "--logging.file.name=/app/logs/application.log"]