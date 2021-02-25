# Kafka Real Time Pipeline & Streaming Application

## About

This is a Kafka project written in purely in Java. It consists of: 
* Data Generator which generates AVRO format data
* Producer which sends this data to a Kafka topic
* Consumer which reads the data, deserializes and stores on your local machine directory "data".
* Kafka Streams application which reads the data, creates a tumbling 1 minute window and creates an aggregate to see how many events were sent to any of the postcodes in that time window. This aggregate is then sent back to a Kafka topic.
* Consumer which reads the data coming from Kafka Streams application and stores the output on your local machine directory "data".

All components have their configuration files in the folder "configuration" so no code changes are needed if we want to change any basic settings like input/output topics, file names, hosts etc.

Note: Ideally this project would be split into different modules/gradle builds but for the sake of simplifying the review process it is structured this way to have all the code in one location.

## Architecture

![alt text](https://github.com/mmaaartin/checkout-kafka/blob/main/images/architecture.png "Architecture")

**Note: In the root directory, folder named "Data" you can find data output files with sample few records left there to review the file structure.**

## To run the project

You will need Docker installed on your machine to run this.

1) Clone the repository

2) run

```
docker-compose up -d
```

This will spin out five docker containers with Zookeeper, Kafka, Schema Registry, Java machine to run the code and Kafdrop for GUI.

3) Run
```
docker ps
```
This will show the created Docker images. Select and copy the container_id associated with this image: "checkout_java-machine".

![alt text](https://github.com/mmaaartin/checkout-kafka/blob/main/images/screenshot.png "Screenshot for docker ps")

4) Run by replacing CONTAINER_ID with the id from the previous step. This will connect you to the instance with the Java code inside.

```
docker exec -it CONTAINER_ID /bin/sh; exit
```
5) Once connected just run the command below which will start the producer, two consumers and a Kafka streams application. 

```
./launch.sh
```
6) This will land the files to the folder "data". With two file outputs page-views-raw.out and page-views-agg.out
