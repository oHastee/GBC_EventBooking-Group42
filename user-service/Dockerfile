#-------------
# Build Stage
#-------------
FROM gradle:8-jdk23 AS builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle build -x test


#-----------
# Package
#-----------
FROM openjdk:23-jdk

# Create an app directory
RUN mkdir /app

# Copy the built jar from the builder stage to the app directory
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/user-service.jar

# Set environment variables for database configuration if needed
ENV POSTGRES_DB=user-service \
    POSTGRES_USER=admin \
    POSTGRES_PASSWORD=password

# Expose the application port
EXPOSE 8089

# Start the application
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]
