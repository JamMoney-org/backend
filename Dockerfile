# ─────────────────────────────
# 1) BUILD STAGE
# ─────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# (선택) 네이티브 빌드 도구가 필요하면 꺼내세요
# RUN apk add --no-cache bash

# 프로젝트 소스 복사
COPY . .

# gradlew 실행 권한
RUN chmod +x ./gradlew

# 테스트는 스킵하고 JAR 빌드
RUN ./gradlew bootJar -x test


# ─────────────────────────────
# 2) RUNTIME STAGE
# ─────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 8080

# Chromium & Chromedriver 및 필수 라이브러리 설치
# - chromium, chromium-chromedriver: 브라우저/드라이버
# - nss, freetype, harfbuzz, fontconfig, ttf-freefont, ttf-dejavu: 폰트/텍스트 렌더링
# - alsa-lib: 일부 오디오 의존성 (필요 시)
RUN apk add --no-cache \
      chromium \
      chromium-chromedriver \
      nss \
      freetype \
      harfbuzz \
      fontconfig \
      ttf-freefont \
      ttf-dejavu \
      alsa-lib

# 환경변수 (일부 라이브러리/테스트 러너가 참조)
ENV CHROME_BIN=/usr/bin/chromium \
    CHROMEDRIVER_BIN=/usr/bin/chromedriver

# 빌드 산출물 복사
COPY --from=builder /app/build/libs/*.jar ./app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
