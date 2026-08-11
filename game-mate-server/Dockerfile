FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy

RUN useradd --system --uid 10001 --create-home gamemate
WORKDIR /app
COPY --from=builder /workspace/target/game-mate-server-*.jar /app/game-mate-server.jar
RUN mkdir -p /data/game-mate/uploads && chown -R gamemate:gamemate /app /data/game-mate

USER gamemate
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Djava.security.egd=file:/dev/urandom", "-jar", "/app/game-mate-server.jar"]

