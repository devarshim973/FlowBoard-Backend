FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN mvn -B -pl notification_service -am clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/notification_service/target/notification_service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
