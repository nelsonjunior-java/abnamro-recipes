# Use an official Maven image to build the JAR
FROM maven:3.8.5-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files to the container
COPY pom.xml ./
COPY src ./src

# Build the application JAR file using Maven
RUN mvn clean package -DskipTests

# Use an official OpenJDK 17 runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the application JAR file from the Maven build stage
COPY --from=build /app/target/recipes-api-*SNAPSHOT.jar app.jar

# Expose the port your application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
