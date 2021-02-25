package com.data.traffic.stream;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import com.data.traffic.avro.PageView;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

public class StreamingAggregatorPageView {
  private static final Logger logger = LoggerFactory.getLogger(StreamingAggregatorPageView.class);

  private SpecificAvroSerde<PageView> pageViewSerde(final Properties envProps) {
    final SpecificAvroSerde<PageView> serde = new SpecificAvroSerde<>();
    Map<String, String> config = new HashMap<>();
    config.put(SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));
    serde.configure(config, false);
    return serde;
  }

  public Topology buildTopology(Properties envProps,
                                final SpecificAvroSerde<PageView> pageViewSerde) {
    final StreamsBuilder builder = new StreamsBuilder();

    final String inputTopic = envProps.getProperty("input.topic.name");
    final String outputTopic = envProps.getProperty("output.topic.name");

    builder.stream(inputTopic, Consumed.with(Serdes.String(), pageViewSerde))
        // Set key to postcode and value to user_id
        .map((k, v) -> new KeyValue<>(v.getPostcode(), v.getUserId()))
        // Group by postcode
        .groupByKey(Grouped.with(Serdes.String(), Serdes.Integer()))
        // Create a window for 1 minute
        .windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
        // Apply COUNT method
        .count()
        // convert KTable back KStream
        .toStream()
        // Converting Windowed<String>, Long into Strings.
        .map((Windowed<String> key, Long count) -> new KeyValue<>(key.toString(), key.toString() + ": "  + count.toString()))
        // Write to stream specified by outputTopic
        .to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
  }

  public Properties loadProperties(String fileName) throws IOException {
    logger.info("Loading properties");
    Properties props = new Properties();
    FileInputStream input = new FileInputStream(fileName);
    props.load(input);
    input.close();

    return props;
  }

  public static void main(String[] args) throws IOException {
   if (args.length < 1) {
     throw new IllegalArgumentException(
         "This program takes one argument: the path to the configuration file.");
   }
    
    new StreamingAggregatorPageView().execute(args[0]);
  }

  private void execute(final String configPath) throws IOException {
    Properties properties = this.loadProperties(configPath);

    Topology topology = this.buildTopology(properties, this.pageViewSerde(properties));

    final KafkaStreams streams = new KafkaStreams(topology, properties);
    final CountDownLatch latch = new CountDownLatch(1);

    // Attach shutdown handler to catch Control-C.
    Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
      @Override
      public void run() {
        streams.close();
        latch.countDown();
      }
    });

    try {
      logger.info("Shutting down.");
      streams.cleanUp();
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }
}
