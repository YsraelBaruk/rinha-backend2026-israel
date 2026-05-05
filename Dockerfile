FROM azul/zulu-openjdk:25 AS builder

RUN apt-get update && apt-get install -y maven --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

FROM azul/zulu-openjdk:25-jre

WORKDIR /app

COPY --from=builder /build/target/rinha-1.0.0-SNAPSHOT.jar app.jar

COPY src/main/resources/references.json.gz  ./resources/
COPY src/main/resources/mcc_risk.json       ./resources/
COPY src/main/resources/normalization.json  ./resources/

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xms64m", \
  "-Xmx110m", \
  "-XX:+UseSerialGC", \
  "-XX:+TieredCompilation", \
  "-jar", "app.jar"]