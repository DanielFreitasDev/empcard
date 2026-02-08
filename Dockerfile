# Etapa de build com Maven Wrapper
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests clean package

# Etapa final enxuta para execucao
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/empcard-1.0.0.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
