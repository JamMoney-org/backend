FROM openjdk:17-slim
WORKDIR /app
EXPOSE 8080

# 1) Install Debian’s Chromium + matching chromedriver
RUN apt-get update \
 && apt-get install -y \
      chromium \
      chromium-driver \
      fonts-liberation \
      libnss3 libxss1 libasound2 \
      libatk-bridge2.0-0 libgtk-3-0 libgbm1 \
 && rm -rf /var/lib/apt/lists/*

# 2) Point Selenium at the right binaries
ENV CHROME_BIN=/usr/bin/chromium \
    CHROMEDRIVER_BIN=/usr/bin/chromedriver

# 3) Copy in your app jar
COPY Jammoney-0.0.1-SNAPSHOT.jar /app.jar

# 4) Run it
ENTRYPOINT ["java","-jar","/app.jar"]
