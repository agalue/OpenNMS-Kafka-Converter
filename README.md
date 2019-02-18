# OpenNMS-Kafka-Converter
A Kafka Stream application to convert GPB payload from Topic A to JSON into Topic B

This solution requires using the OpenNMS Kafka Producer. This feature can export events, alarms, metrics and nodes from the OpenNMS database to Kafka. All the payloads are stored using Google Protobuf.

Unfortunately, for certain solution like Serverless, JSON (or to be more precise, plain text) is required in order to use Kafka as a trigger once a new message arrive to a given Topic. This is why this tool has been implemented.

This repository also contains a Dockerfile to compile and build an image with the tool, which can be fully customized through environment variables, so the solution can be used with Kubernetes (the sample YAML file is also available).

# Requirements

* Oracle JDK 11
* Maven 3

> This can be compiled and run with Java 8, but the `pom.xml` and the `Dockerfile` would require changes.

# Compilation

```shell
mvn install
```

The generated JAR with dependencies (onejar) contains everything needed to execute the application.

Compilation can also be done with Docker:

```shell
docker build -t agalue/opennms-kafka-converter:1.0.0-SNAPSHOT -f Dockerfile.build .
```

# Usage

```shell
$ java -jar kafka-message-converter-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

For more details:

```shell
$ java -jar kafka-message-converter-1.0.0-SNAPSHOT-jar-with-dependencies.jar -h
Usage: kafka-converter [-hV] [-a=id] -b=server:port [-k=kind] -s=topic -t=topic
                       [-e=param[,param...]]...
  -a, --application-id=id    Application ID. Default: grpc2json
  -b, --bootstrap-servers=server:port
                             Kafka Bootstrap Servers
  -e, --producer-param=param[,param...]
                             Optional Kafka  parameters as comma separated list of
                               key-value pairs.
                             Example: -e max.request.size=5000000,acks=1
  -h, --help                 Show this help message and exit.
  -k, --message-kind=kind    Message Kind: events, alarms, metrics, nodes
                             Default: events
  -s, --source-topic=topic   Source Topic
  -t, --target-topic=topic   Target Topic
  -V, --version              Print version information and exit.
```
