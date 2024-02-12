FROM openjdk:18
WORKDIR /app
COPY ./target/broker-0.0.1-SNAPSHOT.jar /app
EXPOSE 8080
CMD ["java", "-jar", "demo-0.0.1-SNAPSHOT.jar"]