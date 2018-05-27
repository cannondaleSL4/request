#!/usr/bin/env bash

fuser -k 8761/tcp
fuser -k 9098/tcp

cd "$(dirname "$0")"
mvn clean install

chmod +x ./target/fxquotes.request-1.0-SNAPSHOT.jar

../eureka/eureka.sh &
java -Xms256m -Xmx328m -XX:+UseSerialGC -jar ./target/*.jar
