FROM amazoncorretto:21

WORKDIR /app

COPY build/libs/pricer-0.0.1-SNAPSHOT.jar /app/application.jar

ENV PORT 8080
EXPOSE $PORT

CMD ["java", "-Xms64M", "-Xmx512M", "-jar", "/app/application.jar"]
