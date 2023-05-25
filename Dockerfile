FROM adoptopenjdk/openjdk11:alpine-jre
LABEL maintainer="xxx"
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]