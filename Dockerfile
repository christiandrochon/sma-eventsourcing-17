FROM openjdk:21-oracle

# Install netcat
RUN microdnf install -y nc curl

VOLUME /tmp
COPY target/*.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
