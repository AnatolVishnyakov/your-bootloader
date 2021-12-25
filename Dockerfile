FROM openjdk:11
EXPOSE 8080

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN mkdir -p '/home/document/trash'
WORKDIR /app
ARG JAR=*.jar

COPY  /target/$JAR /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]