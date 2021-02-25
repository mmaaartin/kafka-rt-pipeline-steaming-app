#!/bin/bash
@echo off

./gradlew build &
java -jar jars/pageview-producer-app-0.0.1.jar configuration/producerDev.properties &
java -jar jars/pageview-consumer-app-0.0.1.jar configuration/consumerRawDev.properties &
java -jar jars/pageview-steaming-app-0.0.1.jar configuration/streamDev.properties &
java -jar jars/pageview-aggregate-consumer-app-0.0.1.jar configuration/consumerAggregateDev.properties &
