package org.opennms.features.kafka.converter;

import java.util.List;
import java.util.Properties;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name="kafka-converter", mixinStandardHelpOptions=true, version="1.0.0")
public class KafkaConverter implements Runnable {

    enum MessageKind { events, alarms, metrics, nodes };

    @Option(names={"-a","--application-id"}, paramLabel="id", description="Application ID. Default: ${DEFAULT-VALUE}", defaultValue="grpc2json")
    String applicationId;

    @Option(names={"-b","--bootstrap-servers"}, paramLabel="server:port", description="Kafka Bootstrap Servers", required=true)
    String bootstrapServers;

    @Option(names={"-s","--source-topic"}, paramLabel="topic", description="Source Topic", required=true)
    String sourceTopic;

    @Option(names={"-t","--target-topic"}, paramLabel="topic", description="Target Topic", required=true)
    String targetTopic; 

    @Option(names={"-k","--message-kind"}, paramLabel="kind", description="Message Kind: events, alarms, metrics, nodes\nDefault: events")
    MessageKind messageKind = MessageKind.events;

    @Option(names={"-e","--producer-param"}, paramLabel="param", split=",", description="Optional Kafka  parameters as comma separated list of key-value pairs.\nExample: -e max.request.size=5000000,acks=1")
    List<String> kafkaParameters;

    public static void main(String[] args) {
        KafkaConverter app = CommandLine.populateCommand(new KafkaConverter(), args);
        CommandLine.run(app, args);
    }

    protected Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(sourceTopic, Consumed.with(Serdes.String(), Serdes.ByteArray()))
                .mapValues(bytes -> deserialize(bytes))
                .to(targetTopic, Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }

    protected Properties createConfig() {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
        return config;
    }

    private String deserialize(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            switch (messageKind) {
                case events:
                    return JsonFormat.printer().print(OpennmsModelProtos.Event.parseFrom(data));
                case alarms:
                    return JsonFormat.printer().print(OpennmsModelProtos.Alarm.parseFrom(data));
                case nodes:
                    return JsonFormat.printer().print(OpennmsModelProtos.Node.parseFrom(data));
                case metrics:
                    return JsonFormat.printer().print(CollectionSetProtos.CollectionSet.parseFrom(data));
            }
            return null;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        final Properties config = createConfig();
        if (kafkaParameters != null) {
            kafkaParameters.forEach(option -> {
                String[] pair = option.split("=");
                config.setProperty(pair[0], pair[1]);
            });
        }
        KafkaStreams streams = new KafkaStreams(createTopology(), config);
        streams.start();
        streams.localThreadsMetadata().forEach(data -> System.out.println(data));

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}

