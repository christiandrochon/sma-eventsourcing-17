FROM maven:3.6.3-jdk-8-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-oracle
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
