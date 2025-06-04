FROM openjdk:17-slim AS builder
WORKDIR /app

RUN apt-get update \
 && apt-get install -y wget unzip \
 && rm -rf /var/lib/apt/lists/*

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew bootJar -x test

FROM openjdk:17-slim
WORKDIR /app
EXPOSE 8080

RUN apt-get update \
 && apt-get install -y \
      chromium \
      chromium-driver \
      fonts-liberation \
      libnss3 libxss1 libasound2 \
      libatk-bridge2.0-0 libgtk-3-0 libgbm1 \
 && rm -rf /var/lib/apt/lists/*

ENV CHROME_BIN=/usr/bin/chromium \
    CHROMEDRIVER_BIN=/usr/bin/chromedriver

COPY --from=builder /app/build/libs/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
