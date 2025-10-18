# Stage 1: Build the application:
# Use official Gradle image with JDK 21 to build the project
FROM gradle:9.1.0-jdk-21-and-24 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradlew wrapper files and the build script
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Copy the rest of the applocation source code
COPY src ./src

# Run Gradle build command to produce the executable jar
RUN ./gradlew build --no-daemon

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy only the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose porth the application will run on
EXPOSE 8080

# The command to run when container starts
ENTRYPOINT ["java", "-jar", "app.jar"]