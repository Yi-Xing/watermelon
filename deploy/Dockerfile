#  21 的运行时镜像
FROM eclipse-temurin:21-jre

# 创建一个目录用于存放 JAR 包
WORKDIR /app

# 只复制 JAR 文件，其他无关文件不拷贝
COPY watermelon-test.jar app.jar

# 声明暴露端口（可选）
EXPOSE 8080

# 容器启动后执行的命令
ENTRYPOINT ["java", "-jar", "app.jar"]

# 构建镜像命令 . 是Dockerfile文件目录  --platform=linux/amd64
# docker build -t watermelon:1.0.0 .
# docker buildx build -t <镜像名称>:<标签> <Dockerfile文件目录>
# 执行目录的文件
#  Dockerfile
#  watermelon-test.jar
