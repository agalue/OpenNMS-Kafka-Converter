FROM maven:3-jdk-8 as build
WORKDIR /app
COPY . .
RUN mvn -DskipTests clean install

FROM openjdk:8-slim
ENV VERSION 1.0.0-SNAPSHOT
WORKDIR /app
COPY --from=build /app/target/kafka-message-converter-${VERSION}-jar-with-dependencies.jar kafka-message-converter.jar
CMD java ${JAVA_OPTS} -jar kafka-message-converter.jar -b ${BOOTSTRAP_SERVERS} -a ${APP_ID} -k ${SOURCE_KIND} -s ${SOURCE_TOPIC} -t ${DEST_TOPIC}
