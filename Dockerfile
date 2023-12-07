FROM openjdk:17-slim

COPY . /
RUN sed -i -e '/composeApp/d' settings.gradle.kts
RUN ./gradlew :server:build

EXPOSE 8080
ENTRYPOINT ["java","-jar","./server/build/libs/server-all.jar"]
