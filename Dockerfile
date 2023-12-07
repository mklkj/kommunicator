FROM openjdk:17-slim

COPY . /
RUN ./gradlew :server:build

EXPOSE 8080
ENTRYPOINT ["java","-jar","./server/build/libs/server-all.jar"]
