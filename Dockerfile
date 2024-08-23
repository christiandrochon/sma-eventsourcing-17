FROM openjdk:21-oracle
VOLUME /tmp
COPY target/*.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
