package com.data.traffic.consumer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class WriteAvroToFileConsumerRecordsHandler implements ConsumerRecordsHandler<String, GenericRecord> {
    private final Path path;

    public WriteAvroToFileConsumerRecordsHandler(final Path path) {
        this.path = path;
    }

    @Override
    public void process(ConsumerRecords<String, GenericRecord> consumerRecords) {
        final List<String> valueList = new ArrayList<>();
        consumerRecords.forEach(record -> valueList.add(record.value().toString()));
        if (!valueList.isEmpty()) {
            try {
                Files.write(path, valueList, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
