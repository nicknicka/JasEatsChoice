# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 安装 curl（健康检查用）
RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar app.jar

EXPOSE 7777 11277

ENTRYPOINT ["java", \
    "-jar", "app.jar", \
    "-Dfile.encoding=UTF-8", \
    "-Dsun.jnu.encoding=UTF-8"]
