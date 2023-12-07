FROM openjdk:17-slim

COPY server /
RUN ./gradlew :server:build

EXPOSE 8000
ENTRYPOINT ["java","-jar","./server/build/libs/server-all.jar"]
