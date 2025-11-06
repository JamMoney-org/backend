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

# Chromium & Chromedriver + 필수 런타임/폰트/타임존
# (Ubuntu 계열에서 chromium이 snap인 경우가 있는데, temurin jre는 Debian 기반이므로 apt 패키지로 설치 가능)
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      chromium chromium-driver \
      libnss3 libxss1 libasound2 libatk-bridge2.0-0 libgtk-3-0 libgbm1 \
      fonts-noto-cjk fonts-dejavu fontconfig ca-certificates tzdata \
 && rm -rf /var/lib/apt/lists/*

# 경로/환경 변수 (배포판에 따라 chromedriver 경로가 다를 수 있어 아래 우선순위로 체크)
# Debian: /usr/bin/chromium, /usr/bin/chromedriver 가 일반적
ENV CHROME_BIN=/usr/bin/chromium \
    CHROMEDRIVER_BIN=/usr/bin/chromedriver \
    XDG_CACHE_HOME=/dev/shm/xdg-cache \
    CHROME_USER_DATA_DIR=/dev/shm/chrome-profile \
    CHROME_DISK_CACHE_DIR=/dev/shm/chrome-cache \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8" \
    TZ=Asia/Seoul

# 앱 복사
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["java","-jar","app.jar"]
