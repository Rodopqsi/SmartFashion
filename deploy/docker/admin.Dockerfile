ARG BUILD_FROM_HOST=false
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /build
COPY ../admin/ ./

RUN if [ "$BUILD_FROM_HOST" = "true" ]; then echo "Using host-provided build artifacts"; else mvn -f pom.xml -DskipTests package dependency:copy-dependencies -DoutputDirectory=target/dependency -e; fi

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /build/target/classes ./classes
COPY --from=build /build/target/dependency ./dependency

# Ensure static resources are present in final image (copy from target/classes/static if produced)
COPY --from=build /build/target/classes/static ./classes/static

COPY --from=build /build/target/*.jar ./app.jar
EXPOSE 8081

CMD ["sh", "-c", "java -cp 'classes:dependency/*' com.smarthfashion.admin.AdminApplication"]
