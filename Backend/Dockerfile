# Step 1: Build the app using JDK 21
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar -x test

# Step 2: Run the app using a slim JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# This copies the jar generated in step 1
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]