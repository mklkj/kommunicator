FROM openjdk:17-slim

COPY . /
RUN sed -i -e '/composeApp/d' settings.gradle.kts
RUN sed -i -e '/iosX64/d' -e '/iosArm64/d' -e '/iosSimulatorArm64/d' shared/build.gradle.kts
RUN ./gradlew :server:shadowjar --no-daemon

EXPOSE 8080
ENTRYPOINT ["java","-jar","./server/build/libs/server-all.jar"]
