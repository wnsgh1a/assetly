FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S assetly && adduser -S assetly -G assetly
WORKDIR /app
COPY --from=build /workspace/target/assetly-backend-*.jar app.jar
RUN chown -R assetly:assetly /app

USER assetly
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
