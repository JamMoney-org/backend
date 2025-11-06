# ─────────────────────────────
# 1) BUILD STAGE
# ─────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY . .
RUN chmod +x ./gradlew
# 캐시 빠르게 하려면 --no-daemon 옵션 고려
RUN ./gradlew bootJar -x test

# ─────────────────────────────
# 2) RUNTIME STAGE
# ─────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 8080

# Chromium & Chromedriver + 필수 폰트/라이브러리
# - headless 환경이므로 udev/mesa 계열은 제거하여 슬림화
RUN apk update && apk add --no-cache \
      chromium \
      chromium-chromedriver \
      nss \
      freetype \
      harfbuzz \
      fontconfig \
      ttf-freefont \
      ttf-dejavu \
      noto-fonts-cjk

# Chrome 경로
ENV CHROME_BIN=/usr/bin/chromium \
    CHROMEDRIVER_BIN=/usr/bin/chromedriver

# 캐시/유저데이터를 RAM(tmpfs)로 유도 → I/O wait 감소
# (코드에서도 --user-data-dir을 주지만, 기본 캐시까지 잡아주면 더 안정)
ENV XDG_CACHE_HOME=/dev/shm/xdg-cache \
    CHROME_USER_DATA_DIR=/dev/shm/chrome-profile \
    CHROME_DISK_CACHE_DIR=/dev/shm/chrome-cache

# JVM 기본 옵션: 컨테이너 메모리 인지 + UTF-8 로깅/파싱
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"

# 앱 복사 (산출물 1개 가정, 필요시 패턴 좁히기)
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar ./app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
