package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.File;
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
        if (driverPath == null || driverPath.isBlank()) {
            driverPath = "/usr/bin/chromedriver";
        }
        System.setProperty("webdriver.chrome.driver", driverPath);
        log.info("[ChromeDriver 경로 설정 완료] {}", driverPath);
    }

    public List<NewsRequestDto> fetchTodayNews() {
        log.info("[크롤링 시작]");
        List<NewsRequestDto> newsList = new ArrayList<>();
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();

            // Chromium 바이너리 경로 설정
            String chromeBin = System.getenv("CHROME_BIN");
            if (chromeBin == null || chromeBin.isBlank()) {
                chromeBin = new File("/usr/bin/chromium").exists()
                        ? "/usr/bin/chromium"
                        : "/usr/bin/chromium-browser";
            }
            options.setBinary(chromeBin);
            log.info("[Chrome 바이너리 경로 사용] {}", chromeBin);

            options.addArguments(
                    "--headless=new",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-gpu",
                    "--use-gl=swiftshader",
                    "--use-angle=swiftshader",
                    "--disable-features=Vulkan,UseDBus",
                    "--remote-debugging-port=0",
                    "--window-size=1280,800",
                    "--user-data-dir=/tmp/chrome-data",
                    "--lang=ko-KR",
                    "--disable-blink-features=AutomationControlled",
                    "user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36"
            );

            options.setPageLoadStrategy(PageLoadStrategy.EAGER);

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(25));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(20));

            // 크롤링 로직
            driver.get("https://biz.chosun.com/finance/");

            WebDriverWait waitMain = new WebDriverWait(driver, Duration.ofSeconds(25));
            waitMain.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("a.story-card__headline")));

            int count = driver.findElements(By.cssSelector("a.story-card__headline")).size();
            log.info("[메인] 검색된 기사 개수: {}", count);

            List<String> links = driver.findElements(By.cssSelector("a.story-card__headline")).stream()
                    .map(e -> e.getAttribute("href"))
                    .filter(h -> h != null && h.startsWith("http"))
                    .toList();

            for (int idx = 0; idx < links.size() && newsList.size() < 3; idx++) {
                String link = links.get(idx);
                log.info("[{}] 상세 페이지 이동 → {}", idx, link);
                driver.navigate().to(link);

                WebDriverWait waitDetail = new WebDriverWait(driver, Duration.ofSeconds(20));
                try {
                    String title = waitDetail.until(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.cssSelector("h1.article-header__headline"))).getText();

                    String content = waitDetail.until(
                                    ExpectedConditions.presenceOfAllElementsLocatedBy(
                                            By.cssSelector("p.article-body__content"))).stream()
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
                    log.warn("[{}] 요소를 찾지 못해 스킵합니다. ({})", idx, te.getMessage());
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            log.error("[크롤링 전체 실패] {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
        } finally {
            if (driver != null) driver.quit();
        }

        log.info("[최종] 크롤링된 뉴스 개수: {}", newsList.size());
        return newsList;
    }
}
