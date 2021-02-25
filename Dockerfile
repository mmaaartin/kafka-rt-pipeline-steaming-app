FROM java:8
WORKDIR /app

RUN mkdir data/

COPY jars/ jars
COPY launch.sh launch.sh
COPY configuration/ configuration
