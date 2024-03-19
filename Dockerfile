# Use a base image with Java installed
FROM openjdk:11-jre-slim

# Install Mosquitto server and client
RUN apt-get update && apt-get install -y mosquitto

# Copy your Java application JAR file to the container
COPY your-application.jar /app/your-application.jar

# Copy Mosquitto configuration file
COPY mosquitto.conf /etc/mosquitto/mosquitto.conf

# Copy environment variables from .env file
COPY .env /app/.env

# Source environment variables
RUN . /app/.env

# Mount specific directories using environment variables
VOLUME $LOG_DATA_DIR
VOLUME $LOGGER_APP_DIR
VOLUME $LOGS_DIR

# Redirect stdout and stderr of Mosquitto broker to mosquitto.log
RUN mkdir -p $LOGS_DIR && touch $LOGS_DIR/mosquitto.log && ln -sf /proc/1/fd/1 $LOGS_DIR/mosquitto.log && ln -sf /proc/1/fd/2 $LOGS_DIR/mosquitto.log

# Set working directory
WORKDIR /app

# Start Mosquitto broker and then run Java application
CMD ["sh", "-c", "mosquitto -c /etc/mosquitto/mosquitto.conf & java -jar your-application.jar 1>$LOGS_DIR/seabrief.log 2>&1"]