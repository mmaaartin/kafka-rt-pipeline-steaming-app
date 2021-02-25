package com.data.traffic.consumer;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class RawConsumerPageView {
    private static final Logger logger = LoggerFactory.getLogger(RawConsumerPageView.class);

    private volatile boolean keepConsuming = true;
    private final ConsumerRecordsHandler<String, GenericRecord> recordsHandler;
    private final Consumer<String, GenericRecord> consumer;

    public RawConsumerPageView(final Consumer<String, GenericRecord> consumer,
                               final ConsumerRecordsHandler<String, GenericRecord> recordsHandler) {
        this.consumer = consumer;
        this.recordsHandler = recordsHandler;
    }

    public void runConsume(final Properties consumerProps) {
        try {
            consumer.subscribe(Collections.singletonList(consumerProps.getProperty("input.topic.name")));
            while (keepConsuming) {
                logger.info("Polling for messages");
                final ConsumerRecords<String, GenericRecord> consumerRecords = consumer.poll(Duration.ofSeconds(1));
                logger.info("Received " + consumerRecords.count() + " records");
                recordsHandler.process(consumerRecords);
                consumer.commitSync();
                logger.info("Records processed. Offset committed.");
            }
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        keepConsuming = false;
    }

    public static Properties loadProperties(String fileName) throws IOException {
        logger.info("Setting up properties...");
        final Properties properties = new Properties();
        final FileInputStream input = new FileInputStream(fileName);
        properties.load(input);
        input.close();
        logger.info("Properties set");
        return properties;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "This program takes one argument: the path to the configuration file.");
        }

        final Properties kafkaProps = RawConsumerPageView.loadProperties(args[0]);
        final String filePath = kafkaProps.getProperty("file.path");
        final Consumer<String, GenericRecord> consumer = new KafkaConsumer<>(kafkaProps);
        final ConsumerRecordsHandler<String, GenericRecord> recordsHandler = new WriteAvroToFileConsumerRecordsHandler(Paths.get(filePath));
        final RawConsumerPageView consumerApp = new RawConsumerPageView(consumer, recordsHandler);

        Runtime.getRuntime().addShutdownHook(new Thread(consumerApp::shutdown));

        consumerApp.runConsume(kafkaProps);
    }
}
