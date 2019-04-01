FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
EXPOSE  8761 9097

ENTRYPOINT ["java","-jar","/app.jar"]

# docker build -t request .
# docker run -p 9098:9098 --net=host request