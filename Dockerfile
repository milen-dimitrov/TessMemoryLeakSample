FROM maven:3.6.1-jdk-8-alpine AS MAVEN_BUILD

WORKDIR /app

COPY pom.xml /app
RUN mvn clean package

COPY ./src ./src/
RUN mvn clean package

FROM debian:bullseye

RUN apt-get update && \
    apt-get install -y openjdk-11-jdk

RUN apt-get update && \
    apt-get install -y libgs9 liblept5 libtesseract4 tesseract-ocr-deu tesseract-ocr-eng

WORKDIR /app/target
COPY sample.pdf /app/target

COPY --from=MAVEN_BUILD ./app/target/ /app/target/

# set the startup command to execute the jar
CMD ["java", "-jar", "./sample-1.jar"]



