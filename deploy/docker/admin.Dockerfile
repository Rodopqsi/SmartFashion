FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /build
COPY ../admin/ ./
# Build the project and copy dependencies into target/dependency
RUN mvn -f pom.xml -DskipTests package dependency:copy-dependencies -DoutputDirectory=target/dependency -e

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy compiled classes and dependency jars
COPY --from=build /build/target/classes ./classes
COPY --from=build /build/target/dependency ./dependency
# Also copy the generated jar as backup (if exists)
COPY --from=build /build/target/*.jar ./app.jar
EXPOSE 8080
# Run using the classes directory and the copied dependency jars on the classpath
CMD ["sh", "-c", "java -cp 'classes:dependency/*' com.smarthfashion.admin.AdminApplication"]

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar ./app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
