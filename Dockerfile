FROM openjdk:11
EXPOSE 8080

ENV LANG ru_RU.UTF-8
ENV LANGUAGE ru_RU:ru
ENV LC_LANG ru_RU.UTF-8
ENV LC_ALL ru_RU.UTF-8

RUN mkdir -p '/home/document/trash'
WORKDIR /app
ARG JAR=*.jar

COPY  /target/$JAR /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]