# Stage: build application JAR
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /application
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -B
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw package -DskipTests -Dspotless.check.skip=true -B

# Stage: extract Spring Boot application layers
FROM eclipse-temurin:25-jre-alpine AS layers
WORKDIR /application
COPY --from=builder /application/target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# Stage: final runtime image
FROM eclipse-temurin:25-jre-alpine

VOLUME /tmp

# Configure non-root user
RUN adduser -S spring-user
USER spring-user

WORKDIR /application

# Copy Spring Boot application layers
COPY --from=layers /application/extracted/dependencies/ ./
COPY --from=layers /application/extracted/spring-boot-loader/ ./
COPY --from=layers /application/extracted/snapshot-dependencies/ ./
COPY --from=layers /application/extracted/application/ ./

EXPOSE 10000
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher --server.port=${PORT:-10000}"]
