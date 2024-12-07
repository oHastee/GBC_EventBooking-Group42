#------------
#Build Stage
#---------------

FROM gradle:8-jdk23 AS builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src


RUN gradle build -x test


#-------------
# Package
#-------------
FROM openjdk:23-jdk

RUN mkdir /app

COPY --from=builder /home/gradle/src/build/libs/*.jar /app/room-service.jar

ENV POSTGRES_USER=admin \
    POSTGRES_PASSWORD=password \
    POSTGRES_DB=room-service

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/room-service.jar" ]