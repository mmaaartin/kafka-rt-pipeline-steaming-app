package com.data.traffic.consumer;

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

public class AggregateConsumerPageView {
    private static final Logger logger = LoggerFactory.getLogger(AggregateConsumerPageView.class);

    private volatile boolean keepConsuming = true;
    private final ConsumerRecordsHandler<String, String> recordsHandler;
    private final Consumer<String, String> consumer;

    public AggregateConsumerPageView(final Consumer<String, String> consumer,
                                     final ConsumerRecordsHandler<String, String> recordsHandler) {
        this.consumer = consumer;
        this.recordsHandler = recordsHandler;
    }

    public void runConsume(final Properties consumerProps) {
        try {
            consumer.subscribe(Collections.singletonList(consumerProps.getProperty("input.topic.name")));
            while (keepConsuming) {
                final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
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

        final Properties kafkaProps = AggregateConsumerPageView.loadProperties(args[0]);
        final String filePath = kafkaProps.getProperty("file.path");
        final Consumer<String, String> consumer = new KafkaConsumer<>(kafkaProps);
        final ConsumerRecordsHandler<String, String> recordsHandler = new WriteStringToFileConsumerRecordsHandler(Paths.get(filePath));
        final AggregateConsumerPageView consumerApp = new AggregateConsumerPageView(consumer, recordsHandler);

        Runtime.getRuntime().addShutdownHook(new Thread(consumerApp::shutdown));

        consumerApp.runConsume(kafkaProps);
    }
}
