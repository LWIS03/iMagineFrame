FROM maven:3-eclipse-temurin-21 AS build
LABEL authors="s0192454 (MDB)"

ARG PACKAGE_MODE=production

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean -Dmaven.test.skip=true -P$PACKAGE_MODE -D spring.profiles.active=$PACKAGE_MODE package

FROM openjdk:21-oracle
LABEL authors="s0192454 (MDB)"

ENV IMF_DB_USER=admin
ENV IMF_DB_PW=admin
ENV IMF_DB_NAME=imf_db
ENV IMF_DB_HOST=localhost
ENV IMF_FRONTEND_URL=http://localhost:8080

WORKDIR /app

COPY --from=build /app/target/*.jar imagineframe_backend.jar

EXPOSE 8080

CMD ["java", "-jar", "imagineframe_backend.jar"]
