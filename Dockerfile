# ── BUILD ──
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# ── RUNTIME ──
FROM eclipse-temurin:17-jre
WORKDIR /app
EXPOSE 8080

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      chromium chromium-driver \
      fonts-noto-cjk fonts-noto-core \
      fonts-dejavu \
      ca-certificates tzdata \
 && rm -rf /var/lib/apt/lists/*

ENV CHROME_BIN=/usr/bin/chromium \
    CHROMEDRIVER_BIN=/usr/bin/chromedriver \
    TZ=Asia/Seoul \
    XDG_CACHE_HOME=/dev/shm/xdg-cache \
    CHROME_USER_DATA_DIR=/dev/shm/chrome-profile \
    CHROME_DISK_CACHE_DIR=/dev/shm/chrome-cache \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
