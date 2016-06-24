FROM anapsix/alpine-java:jdk8

ENV MAVEN_VERSION 3.3.3

RUN wget http://ftp.fau.de/apache/maven/maven-3/3.3.1/binaries/apache-maven-3.3.1-bin.tar.gz \
  && tar -zxvf apache-maven-3.3.1-bin.tar.gz \
  && rm apache-maven-3.3.1-bin.tar.gz \
  && mv apache-maven-3.3.1 /usr/lib/mvn

ENV MAVEN_HOME /usr/share/maven
ENV JAVA=$JAVA_HOME/bin
ENV M2_HOME=/usr/lib/mvn
ENV M2=$M2_HOME/bin
ENV PATH $PATH:$JAVA_HOME:$JAVA:$M2_HOME:$M2

ADD ./ /opt/kafka-websocket

WORKDIR /opt/kafka-websocket

RUN mvn package

CMD java -jar target/kafka-websocket-0.10.0.0-shaded.jar
