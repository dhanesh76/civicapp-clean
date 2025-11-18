FROM eclipse-temurin:17-jdk

# Copy the built JAR file into the container
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
