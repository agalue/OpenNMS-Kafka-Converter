FROM openjdk:11-jre-slim
ENV VERSION 1.0.0-SNAPSHOT
LABEL maintainer="Alejandro Galue <agalue@opennms.org>" name="Kafka Message Converter" version="${VERSION}"
WORKDIR /app
RUN groupadd -r opennms && useradd -r -g opennms opennms
USER opennms
COPY ./target/kafka-message-converter-${VERSION}-jar-with-dependencies.jar kafka-message-converter.jar
COPY ./docker-entrypoint.sh .
ENTRYPOINT [ "/app/docker-entrypoint.sh" ]
