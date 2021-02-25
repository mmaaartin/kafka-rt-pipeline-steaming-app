package com.data.traffic.producer;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ProducerPageView {
    private static final Logger logger = LoggerFactory.getLogger(ProducerPageView.class);

    private final Producer<String, GenericRecord> producer;
    final String outTopic;

    public ProducerPageView(final Producer<String, GenericRecord> producer, final String topic) {
        this.producer = producer;
        outTopic = topic;
    }

    public void produce(final GenericRecord message) {
        logger.info(String.valueOf(message));
        producer.send(new ProducerRecord<>(outTopic, message), (recordMetadata, e) -> {
            if (e == null) {
                // the record was successfully sent
                logger.info("Received new metadata. \n" +
                        "Topic:" + recordMetadata.topic() + "\n" +
                        "Partition: " + recordMetadata.partition() + "\n" +
                        "Offset: " + recordMetadata.offset() + "\n" +
                        "Timestamp: " + recordMetadata.timestamp());
            } else {
                logger.error("Error while producing", e);
            }
        });
    }

    public void shutdown() {
        producer.close();
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

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "This program takes one argument: the path to the configuration file.");
        }

        final Properties kafkaProps = ProducerPageView.loadProperties(args[0]);
        final String topic = kafkaProps.getProperty("output.topic.name");
        final Producer<String, GenericRecord> producer = new KafkaProducer<>(kafkaProps);
        final ProducerPageView producerApp = new ProducerPageView(producer, topic);

        // I would not be using infinite loop here normally, but I added it here to not to overcomplicate
        // DataGenerotor class and easily show results.
        while (true) {
            GenericRecord record = DataGenerator.generateRecord();

            producerApp.produce(record);
            TimeUnit.SECONDS.sleep(1);
        }
        // producerApp.shutdown();
    }
}
