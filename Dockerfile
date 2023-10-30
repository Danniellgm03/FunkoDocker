FROM gradle:jdk17 as build

WORKDIR /app

COPY build.gradle.kts .
COPY gradlew .
COPY gradle gradle
COPY src src
COPY cert cert

RUN chmod +x /app/cert/keys.sh && /app/cert/keys.sh

RUN ./gradlew shadowJar

FROM openjdk:17-jdk AS run

WORKDIR /app

COPY src/main/resources/config.properties src/main/resources/
COPY src/main/resources/data/* src/main/resources/data/
COPY cert/* cert/
COPY src/main/resources/server.properties src/main/resources/
COPY src/main/resources/client.properties src/main/resources/
COPY --from=build /app/build/libs/*.jar my-app.jar

ENTRYPOINT ["java", "-jar", "/app/my-app.jar"]