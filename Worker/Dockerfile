FROM gradle:jdk17-alpine as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM openjdk:17-jdk-alpine
COPY --from=builder /home/gradle/src/build/libs/Worker.jar /app/Worker.jar
WORKDIR /app
EXPOSE 8001
CMD ["java", "-jar", "Worker.jar"]