FROM gradle:9.3.1-jdk25-alpine AS builder

WORKDIR /app

COPY gradlew ./
RUN chmod +x gradlew

COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN ./gradlew --no-daemon dependencies || true

COPY src src
RUN ./gradlew --no-daemon clean bootJar

RUN java -Djarmode=tools -jar build/libs/*.jar extract --layers --launcher --destination extracted


FROM eclipse-temurin:25-jre-alpine

COPY cerberus-root-ca.crt /usr/local/share/ca-certificates/

RUN update-ca-certificates

RUN keytool -importcert -noprompt \
    -trustcacerts \
    -alias cerberus-root-ca \
    -file /usr/local/share/ca-certificates/cerberus-root-ca.crt \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit

RUN addgroup -g 1000 bankingbe && adduser -u 1000 -S apigw -G bankingbe

WORKDIR /app

COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

RUN chown -R 1000:1000 /app

USER apigw

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:InitialRAMPercentage=50 \
               -XX:MaxMetaspaceSize=256m \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=250 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/heapdumps/heapdump-%p-%t.hprof \
               -XX:ErrorFile=/heapdumps/hs_err_pid%p_%t.log \
               -XX:+ExitOnOutOfMemoryError \
               -Xlog:gc*:file=/heapdumps/gc.log:time,uptime,level,tags:filecount=5,filesize=20M"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]