FROM dockerfile/java:openjdk-7-jdk

RUN apt-get update && apt-get install -y maven

ADD ./ /opt/kafka-websocket

WORKDIR /opt/kafka-websocket

RUN mvn package

CMD java -jar target/kafka-websocket-0.8.1-SNAPSHOT-shaded.jar

