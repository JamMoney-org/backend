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
# ──────────────── RUNTIME ───────────────
FROM eclipse-temurin:17-jre
WORKDIR /app
EXPOSE 8080

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      ca-certificates tzdata fonts-noto-cjk fonts-dejavu fontconfig \
 && rm -rf /var/lib/apt/lists/*

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8" \
    TZ=Asia/Seoul

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar ./app.jar
ENTRYPOINT ["java","-jar","app.jar"]

