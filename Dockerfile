FROM eclipse-temurin:21-jre AS auth-service
WORKDIR /app
COPY auth-service/target/auth-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre AS notification-service
WORKDIR /app
COPY notification_service/target/notification_service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre AS comment-service
WORKDIR /app
COPY comment-service/target/comment-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre AS workspace-service
WORKDIR /app
COPY workspace-service/target/workspace-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre AS board-service
WORKDIR /app
COPY board-service/target/board-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre AS list-service
WORKDIR /app
COPY list-service/target/list-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre AS card-service
WORKDIR /app
COPY card-service/target/card-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre AS payment-service
WORKDIR /app
COPY payment-service/target/payment-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre AS admin-service
WORKDIR /app
COPY admin-service/target/admin-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre AS flowboard-api-gateway
WORKDIR /app
COPY flowboard-api-gateway/target/flowboard-api-gateway-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre AS flowboard-server
WORKDIR /app
COPY flowboard-server/target/flowboard-server-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
