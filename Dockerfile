FROM openjdk:8-jre-slim

EXPOSE 8080

RUN mkdir /app

COPY jars/ /app/jars
COPY launch.sh /app/launch.sh
COPY configuration/ /app/configuration
