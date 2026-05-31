FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN mkdir -p /data/db && chmod 777 /data/db
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
