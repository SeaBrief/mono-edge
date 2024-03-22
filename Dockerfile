# Start with a base image that includes the Java runtime environment
FROM openjdk:11

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file of your Java application into the container
COPY your-java-app.jar /app/your-java-app.jar

# Mount the database directory, config file directory, and env file
VOLUME /app/database
VOLUME /app/config
VOLUME /app/env

# Set the entry point and command to run your Java application
ENTRYPOINT ["java", "-jar", "/app/your-java-app.jar"]