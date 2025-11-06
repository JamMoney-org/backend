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

    // 셀렉터 상수
    private static final By SEL_MAIN_LINKS  = By.cssSelector("a.story-card__headline");
    private static final By SEL_DETAIL_TITLE = By.cssSelector("h1.article-header__headline");
    private static final By SEL_DETAIL_PARAS = By.cssSelector("p.article-body__content");

    // 본문 수집 정책
    private static final Duration DETAIL_TITLE_WAIT = Duration.ofSeconds(8);
    private static final Duration CONTENT_BUDGET    = Duration.ofSeconds(8);
    private static final int MIN_PARAS = 6;
    private static final int MAX_PARAS = 30;

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

            // 성능/안정화 옵션
            options.addArguments(
                    "--headless=new",
                    "--no-sandbox",
                    "--incognito",

                    // 네트워크/페인트/폰트/플러그인 최소화
                    "--blink-settings=imagesEnabled=false",
                    "--disable-plugins",
                    "--disable-features=Translate,BackForwardCache,DownloadableFonts,PaintHolding,Prewarm",
                    "--disable-background-networking",
                    "--disable-domain-reliability",
                    "--disable-sync",
                    "--no-first-run",
                    "--no-default-browser-check",

                    // 크래시/로그 I/O 감소
                    "--disable-crash-reporter",
                    "--disable-logging",
                    "--log-level=3",

                    // 프로세스/메모리 제한
                    "--renderer-process-limit=2",
                    "--js-flags=--max_old_space_size=64",

                    // 기타
                    "--window-size=1280,800",
                    "--lang=ko-KR",
                    "--disk-cache-size=0",
                    "--media-cache-size=0"
            );

            // 페이지 전체 로딩을 기다리지 않고, 필요한 요소만 직접 대기
            options.setPageLoadStrategy(PageLoadStrategy.NONE);

            driver = new ChromeDriver(options);

            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));

            driver.get("https://biz.chosun.com/finance/");

            WebDriverWait waitMain = new WebDriverWait(driver, Duration.ofSeconds(10));
            waitMain.until(ExpectedConditions.presenceOfElementLocated(SEL_MAIN_LINKS));
            safeWindowStop(driver); // 핵심 요소 확보 후 잔여 로딩 중단

            List<String> links = driver.findElements(SEL_MAIN_LINKS).stream()
                    .map(e -> e.getAttribute("href"))
                    .filter(h -> h != null && h.startsWith("http"))
                    .toList();

            log.info("[메인] 검색된 기사 링크 수: {}", links.size());

            for (int idx = 0; idx < links.size() && newsList.size() < 3; idx++) {
                String link = links.get(idx);
                log.info("[{}] 상세 페이지 이동 → {}", idx, link);

                try {
                    driver.navigate().to(link);

                    // 1) 핵심 요소(제목) 8초 대기
                    WebDriverWait waitDetail = new WebDriverWait(driver, DETAIL_TITLE_WAIT);
                    String title = waitDetail
                            .until(ExpectedConditions.presenceOfElementLocated(SEL_DETAIL_TITLE))
                            .getText();

                    // 2) 본문은 제한된 시간 예산 내 반복 수집
                    String content = extractContentWithBudget(
                            driver, SEL_DETAIL_PARAS, CONTENT_BUDGET, MIN_PARAS, MAX_PARAS);

                    // 3) 남은 네트워크 끊기
                    safeWindowStop(driver);

                    if (content.isBlank()) {
                        log.warn("[{}] 본문이 비어 스킵", idx);
                        continue;
                    }

                    NewsRequestDto dto = new NewsRequestDto();
                    dto.setTitle(title);
                    dto.setContent(content);
                    dto.setSource("조선비즈");
                    dto.setPublishDate(LocalDate.now());
                    newsList.add(dto);

                    log.info("[{}] 크롤링 완료 항목 수: {}", idx, newsList.size());
                } catch (TimeoutException te) {
                    log.warn("[{}] 상세 요소 대기 시간 초과 → 스킵 ({})", idx, te.getMessage());
                    safeWindowStop(driver);
                } catch (Exception ex) {
                    log.warn("[{}] 상세 페이지 처리 예외 → 스킵: {}: {}", idx, ex.getClass().getSimpleName(), ex.getMessage());
                    safeWindowStop(driver);
                }

                // 사이트 부하 방지 (짧게 간격)
                try { Thread.sleep(500); } catch (InterruptedException ignore) {}
            }
        } catch (Exception e) {
            log.error("[크롤링 전체 실패] {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
        } finally {
            safeQuit(driver);
        }

        log.info("[최종] 크롤링된 뉴스 개수: {}", newsList.size());
        return newsList;
    }

    /** 시간 예산 내에서 문단을 점진 수집(스크롤 보정 포함) */
    private String extractContentWithBudget(
            WebDriver driver, By paraSelector,
            Duration budget, int minParas, int maxParas
    ) {
        long deadline = System.nanoTime() + budget.toNanos();
        List<String> lines = new ArrayList<>();
        int scrolls = 0;

        while (System.nanoTime() < deadline) {
            try {
                List<WebElement> paras = driver.findElements(paraSelector);
                lines = paras.stream()
                        .map(WebElement::getText)
                        .filter(s -> s != null && !s.isBlank())
                        .distinct()
                        .limit(maxParas)
                        .toList();
            } catch (Exception ignore) {}

            if (lines.size() >= minParas) break;

            // 지연 로딩 보정: 2~3회 가볍게 스크롤
            if (scrolls < 3) {
                try {
                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 800);");
                } catch (Exception ignore) {}
                scrolls++;
            }

            try { Thread.sleep(400); } catch (InterruptedException ignore) {}
        }

        // 네트워크 중단(광고 등 불필요 리소스 컷)
        safeWindowStop(driver);

        return String.join("\n\n", lines);
    }

    private void safeWindowStop(WebDriver driver) {
        try { ((JavascriptExecutor) driver).executeScript("window.stop();"); }
        catch (Exception ignore) {}
    }

    /** 크롬/드라이버 잔여 프로세스까지 정리 */
    private void safeQuit(WebDriver driver) {
        if (driver == null) return;
        try {
            for (String h : driver.getWindowHandles()) {
                try { driver.switchTo().window(h).close(); } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}

        try { driver.quit(); } catch (Exception ignore) {}

        // 환경에 따라 바이너리명이 다를 수 있어 패턴 확장
        try {
            new ProcessBuilder("sh","-lc",
                    "pkill -f 'chromium|google-chrome|chrome|chromedriver' || true"
            ).start().waitFor();
        } catch (Exception ignore) {}
    }
}
