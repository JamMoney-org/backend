package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FinanceNewsCrawler {

    @PostConstruct
    public void initChromeDriverPath() {
        String driverPath = System.getenv("CHROMEDRIVER_BIN");
        if (driverPath != null && !driverPath.isBlank()) {
            System.setProperty("webdriver.chrome.driver", driverPath);
            log.info("[ChromeDriver 경로 설정 완료] {}", driverPath);
        } else {
            WebDriverManager.chromedriver().setup();
            log.info("[WebDriverManager로 ChromeDriver 다운로드]");
        }
    }

    public List<NewsRequestDto> fetchTodayNews() {
        log.info("[크롤링 시작]");
        List<NewsRequestDto> newsList = new ArrayList<>();
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();

            // ENV 에서 Chromium 경로 읽기
            String chromeBin = System.getenv("CHROME_BIN");
            if (chromeBin != null && !chromeBin.isBlank()) {
                options.setBinary(chromeBin);
                log.info("[Chrome 바이너리 경로 사용] {}", chromeBin);
            } else {
                options.setBinary("/usr/bin/chromium-browser");
                log.info("[Chrome 바이너리 경로 fallback] /usr/bin/chromium-browser");
            }

            options.addArguments(
                    "--headless",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-gpu",
                    "--single-process",
                    "--remote-debugging-port=9222",
                    "--user-data-dir=/tmp"
            );

            driver = new ChromeDriver(options);

            // --- 이하 기존 로직 유지 ---
            driver.get("https://biz.chosun.com/finance/");
            WebDriverWait waitMain = new WebDriverWait(driver, Duration.ofSeconds(20));
            waitMain.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("a.story-card__headline")));
            log.info("[메인] 검색된 기사 개수: {}",
                    driver.findElements(By.cssSelector("a.story-card__headline")).size());

            List<String> links = driver.findElements(By.cssSelector("a.story-card__headline")).stream()
                    .map(e -> e.getAttribute("href"))
                    .filter(h -> h != null && h.startsWith("http"))
                    .toList();

            for (int idx = 0; idx < links.size() && newsList.size() < 3; idx++) {
                String link = links.get(idx);
                log.info("[{}] 상세 페이지 이동 → {}", idx, link);
                driver.navigate().to(link);

                WebDriverWait waitDetail = new WebDriverWait(driver, Duration.ofSeconds(10));
                try {
                    String title = waitDetail.until(
                                    ExpectedConditions.presenceOfElementLocated(
                                            By.cssSelector("h1.article-header__headline")))
                            .getText();

                    String content = waitDetail.until(
                                    ExpectedConditions.presenceOfAllElementsLocatedBy(
                                            By.cssSelector("p.article-body__content")))
                            .stream()
                            .map(WebElement::getText)
                            .reduce((a, b) -> a + "\n\n" + b)
                            .orElse("");

                    NewsRequestDto dto = new NewsRequestDto();
                    dto.setTitle(title);
                    dto.setContent(content);
                    dto.setSource("조선비즈");
                    dto.setPublishDate(LocalDate.now());
                    newsList.add(dto);
                    log.info("[{}] 크롤링 완료 항목 수: {}", idx, newsList.size());
                } catch (TimeoutException te) {
                    log.warn("[{}] 요소를 찾지 못해 스킵합니다.", idx);
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            log.error("[크롤링 전체 실패]", e);
        } finally {
            if (driver != null) driver.quit();
        }

        log.info("[최종] 크롤링된 뉴스 개수: {}", newsList.size());
        return newsList;
    }
}
