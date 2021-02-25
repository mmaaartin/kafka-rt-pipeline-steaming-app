package com.data.traffic.producer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class DataGenerator {

    public DataGenerator() {}

    public static GenericRecord generateRecord() {
        Schema.Parser parser = new Schema.Parser();
        String schemaString = "{\"namespace\": \"com.data.traffic.avro\", " +
                "\"type\": \"record\", " +
                "\"name\": \"PageView\"," +
                "\"fields\": [" +
                "{\"name\": \"user_id\", \"type\": \"int\"}," +
                "{\"name\": \"postcode\", \"type\": \"string\"}," +
                "{\"name\": \"webpage\", \"type\": " + "[\"null\",\"string\"] }, " +
                "{\"name\": \"timestamp\", \"type\": \"long\"} " +
                "]}";
        Schema schema = parser.parse(schemaString);

        GenericRecord record = new GenericData.Record(schema);
        record.put("user_id", getRandomId());
        record.put("postcode", getRandomPostcode());
        record.put("webpage", getRandomWebpage());
        record.put("timestamp", getTimestamp());

        return record;

    }

    static long getTimestamp(){
        return Instant.now().toEpochMilli();
    }

    static int getRandomId() {
        Random rand = new Random();
        return rand.nextInt(100000);
    }

    static String getRandomPostcode() {
        Random rand = new Random();

        List<String> postcodes = Arrays.asList(
                "E1", "E2", "E3", "W1", "W2", "N1",
                "N2", "N3", "WC1", "WC2");

        return postcodes.get(rand.nextInt(postcodes.size()));
    }

    static String getRandomWebpage() {
        Random rand = new Random();

        List<String> webpages = Arrays.asList(
                "www.sample.com/index.html",
                "www.sample.com/about.html",
                "www.sample.com/contacts.html",
                "www.sample.com/shop.html",
                "www.sample.com/download.html"
        );

        return webpages.get(rand.nextInt(webpages.size()));
    }

}
