# ───────────────── BUILD ─────────────────
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# ──────────────── RUNTIME ───────────────
FROM eclipse-temurin:17-jre
WORKDIR /app
EXPOSE 8080

# deps
RUN apt-get update \
 && apt-get install -y --no-install-recommends wget gnupg ca-certificates tzdata fonts-noto-cjk fonts-dejavu fontconfig \
 && rm -rf /var/lib/apt/lists/*

# Google Chrome repo 추가 + 설치
RUN wget -qO- https://dl-ssl.google.com/linux/linux_signing_key.pub \
    | gpg --dearmor -o /usr/share/keyrings/google-linux.gpg \
 && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-linux.gpg] http://dl.google.com/linux/chrome/deb/ stable main" \
    > /etc/apt/sources.list.d/google-chrome.list \
 && apt-get update \
 && apt-get install -y --no-install-recommends google-chrome-stable \
 && rm -rf /var/lib/apt/lists/*

# 환경
ENV CHROME_BIN=/usr/bin/google-chrome \
    XDG_CACHE_HOME=/dev/shm/xdg-cache \
    CHROME_USER_DATA_DIR=/dev/shm/chrome-profile \
    CHROME_DISK_CACHE_DIR=/dev/shm/chrome-cache \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8" \
    TZ=Asia/Seoul

# 앱
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar ./app.jar
ENTRYPOINT ["java","-jar","app.jar"]
