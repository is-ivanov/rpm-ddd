# Stage: build application JAR
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /builder
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -B
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw package -DskipTests -Dspotless.check.skip=true -B

# Stage: extract Spring Boot application layers
FROM eclipse-temurin:25-jre-alpine AS layers
WORKDIR /layers
COPY --from=builder /builder/target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# Stage: final runtime image
FROM eclipse-temurin:25-jre-alpine

VOLUME /tmp

# Configure non-root user
RUN adduser -S spring-user
USER spring-user

WORKDIR /application

# Copy Spring Boot application layers
COPY --from=layers /layers/extracted/dependencies/ ./
COPY --from=layers /layers/extracted/spring-boot-loader/ ./
COPY --from=layers /layers/extracted/snapshot-dependencies/ ./
COPY --from=layers /layers/extracted/application/ ./

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]
