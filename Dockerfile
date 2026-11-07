# ===== Bước 1: build file .jar bằng Maven =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Tải thư viện trước (được cache nếu pom.xml không đổi)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B -DskipTests package

# ===== Bước 2: image chạy, chỉ có JRE + file .jar (nhẹ hơn nhiều) =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Cloud Run truyền cổng qua biến PORT (mặc định 8080)
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75 -jar app.jar"]
