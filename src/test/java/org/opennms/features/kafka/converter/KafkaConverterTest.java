package org.opennms.features.kafka.converter;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;

import org.json.JSONObject;

import org.junit.Assert;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KafkaConverterTest {

    TopologyTestDriver testDriver;

    ConsumerRecordFactory<String, byte[]> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new ByteArraySerializer());

    @Before
    public void setUpTopologyTestDriver() {
        KafkaConverter app = new KafkaConverter();
        app.applicationId = "test";
        app.bootstrapServers = "dummy:1234";
        app.sourceTopic = "test_events";
        app.targetTopic = "test_events_json";
        testDriver = new TopologyTestDriver(app.createTopology(), app.createConfig());
    }

    @After
    public void closeTestDriver() {
        testDriver.close();
    }

    @Test
    public void testConvertion() throws Exception {
        // Create an Event
        OpennmsModelProtos.Event event = OpennmsModelProtos.Event.newBuilder().setId(666).setUei("uei.jigsaw.event").setLogMessage("hello, I want to play a game").build();
        // Send a BGP event
        testDriver.pipeInput(recordFactory.create("test_events", null, event.toByteArray()));
        // Receive a JSON event
        ProducerRecord<String, String> record = testDriver.readOutput("test_events_json", new StringDeserializer(), new StringDeserializer());
        System.out.println(record.value());
        JSONObject json = new JSONObject(record.value());
        Assert.assertEquals(666, json.getInt("id"));
        Assert.assertEquals("uei.jigsaw.event", json.getString("uei"));
    }
}
