FROM openjdk:21-oracle
VOLUME /tmp
COPY target/sma-monolithe-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
