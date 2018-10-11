FROM java:openjdk-7-jdk

RUN apt-get update && apt-get install -y maven

ADD ./ /opt/kafka-websocket

WORKDIR /opt/kafka-websocket

RUN mvn -Dhttps.protocols=TLSv1.2 package

CMD java -jar target/kafka-websocket-0.8.2-SNAPSHOT-shaded.jar

