FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN mvn -B -pl flowboard-server -am clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/flowboard-server/target/flowboard-server-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
