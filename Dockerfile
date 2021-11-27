FROM openjdk:11
EXPOSE 8080
WORKDIR /app
ARG JAR=*.jar

COPY  /target/$JAR /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]