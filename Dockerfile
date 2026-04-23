# 使用 Eclipse Temurin JDK 21 Windows 镜像
FROM eclipse-temurin:21-jdk-windowsservercore-ltsc2022

# 设置工作目录
WORKDIR C:/app

# 复制编译好的 jar 文件
COPY target/javafx-mes-1.0.0.jar .

# 复制数据库文件（如果有）
COPY mes.db .

# 运行命令 - 运行编译后的 Spring Boot 应用
CMD ["java", "-jar", "javafx-mes-1.0.0.jar"]
