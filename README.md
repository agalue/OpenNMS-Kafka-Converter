# OpenNMS-Kafka-Converter
A Kafka Stream application to convert GPB payload from Topic A to JSON into Topic B

This solution requires using the OpenNMS Kafka Producer. This feature can export events, alarms, metrics and nodes from the OpenNMS database to Kafka. All the payloads are stored using Google Protobuf.

Unfortunately, for certain solution like Serverless, JSON (or to be more precise, plain text) is required in order to use Kafka as a trigger once a new message arrive to a given Topic. This is why this tool has been implemented.

This repository also contains a Dockerfile to compile and build an image with the tool, which can be fully customized through environment variables, so the solution can be used with Kubernetes (the sample YAML file is also available).
