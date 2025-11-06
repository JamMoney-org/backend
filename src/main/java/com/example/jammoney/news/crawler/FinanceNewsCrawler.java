package com.example.jammoney.news.crawler;

import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import com.example.jammoney.news.dto.NewsRequestDto;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

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

        // 실행마다 분리된 프로필 디렉토리로 락/잔여파일 이슈 방지
        Path profileDir = null;
        Path cacheDir = null;

        try {
            ChromeOptions options = new ChromeOptions();

            // 1) 크롬 바이너리 경로 (환경변수 우선, 없으면 일반 경로 탐색)
            String chromeBin = System.getenv("CHROME_BIN");
            if (chromeBin != null && !chromeBin.isBlank()) {
                options.setBinary(chromeBin);
                log.info("[Chrome BIN] {}", chromeBin);
            } else if (Files.exists(Paths.get("/usr/bin/chromium"))) {
                options.setBinary("/usr/bin/chromium");
                log.info("[Chrome BIN fallback] /usr/bin/chromium");
            } else {
                options.setBinary("/usr/bin/chromium-browser");
                log.info("[Chrome BIN fallback] /usr/bin/chromium-browser");
            }

            // 2) 헤드리스/안정 옵션
            options.addArguments(
                    "--headless=new",
                    "--no-sandbox",
                    "--disable-gpu",
                    "--disable-extensions",
                    "--disable-features=Translate",
                    "--disable-notifications",
                    "--window-size=1366,900",
                    "--lang=ko-KR",
                    "--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                    "--disable-gpu-shader-disk-cache"
            );
            // 필요할 때만 디버깅 포트 사용
            // options.addArguments("--remote-debugging-port=9222");

            // 3) /dev/shm에 프로필/캐시 경로 고정 → 디스크 I/O↓
            String uuid = UUID.randomUUID().toString();
            profileDir = Paths.get("/dev/shm/chrome-profile-" + uuid);
            cacheDir   = Paths.get("/dev/shm/chrome-cache-" + uuid);
            try {
                Files.createDirectories(profileDir);
                Files.createDirectories(cacheDir);
                options.addArguments("--user-data-dir=" + profileDir.toAbsolutePath());
                options.addArguments("--disk-cache-dir=" + cacheDir.toAbsolutePath());
                log.info("[Chrome profile] {}", profileDir);
                log.info("[Chrome cache]   {}", cacheDir);
            } catch (Exception e) {
                log.warn("[/dev/shm 경로 생성 실패] 기본값 사용 (/tmp)");
            }

            // 4) 페이지 로드 전략: EAGER (전체 로딩 대기 X → 빠름)
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);

            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // 유틸: 문서 준비상태 complete 대기
            WebDriver finalDriver = driver;
            Runnable waitDocReady = () -> {
                try {
                    new WebDriverWait(finalDriver, Duration.ofSeconds(10)).until(d ->
                            "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
                } catch (Exception ignore) {}
            };

            // ============ 메인 페이지 ============ //
            driver.get("https://biz.chosun.com/finance/");
            waitDocReady.run();

            // 쿠키/동의 배너 있을 때만 닫기 (있으면 닫고 없으면 무시)
            try {
                List<By> bannerButtons = List.of(
                        By.cssSelector("button[aria-label*='동의']"),
                        By.xpath("//button[contains(.,'동의')]")
                );
                for (By b : bannerButtons) {
                    List<WebElement> el = driver.findElements(b);
                    if (!el.isEmpty()) { el.get(0).click(); log.info("[쿠키 배너 닫음]"); break; }
                }
            } catch (Exception ignore) {}

            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.story-card__headline")));
            List<String> links = driver.findElements(By.cssSelector("a.story-card__headline")).stream()
                    .map(e -> e.getAttribute("href"))
                    .filter(h -> h != null && h.startsWith("http"))
                    // 금융/증권 섹션 가드(불필요한 상세 진입 방지)
                    .filter(h -> h.contains("/finance/") || h.contains("/stock/"))
                    .distinct()
                    .collect(Collectors.toList());
            log.info("[메인] 링크 수: {}", links.size());

            // ============ 상세 페이지 반복 ============ //
            for (String link : links) {
                if (newsList.size() >= 3) break;
                log.info("[상세 이동] {}", link);

                try {
                    driver.navigate().to(link);
                    waitDocReady.run();

                    // 제목 폴백: 우선순위대로 첫 매칭 텍스트 사용
                    String title = firstNonBlankText(driver,
                            "h1.article-header__headline",
                            "h1.article-title",
                            "h1.headline",
                            "h1"
                    );
                    if (title == null) {
                        title = getMetaContent(driver, "meta[property='og:title']");
                    }
                    if (isBlank(title)) {
                        log.warn("[스킵] 제목 파싱 실패 {}", link);
                        continue;
                    }

                    // 본문 폴백: 여러 후보 셀렉터
                    List<WebElement> paras = firstNonEmptyElements(driver,
                            "p.article-body__content",
                            "div.article-body p",
                            "article p",
                            "div[itemprop='articleBody'] p"
                    );
                    String content = paras.stream()
                            .map(WebElement::getText)
                            .filter(s -> s != null && !s.isBlank())
                            .collect(Collectors.joining("\n\n"));

                    if (isBlank(content)) {
                        log.warn("[스킵] 본문 파싱 실패 {}", link);
                        continue;
                    }

                    // 날짜: 메타 태그 우선, 없으면 오늘
                    LocalDate published = LocalDate.now();
                    String iso = getMetaContent(driver, "meta[property='article:published_time']");
                    if (!isBlank(iso)) {
                        try {
                            published = java.time.OffsetDateTime.parse(iso).toLocalDate();
                        } catch (Exception ignore) {}
                    }

                    NewsRequestDto dto = new NewsRequestDto();
                    dto.setTitle(title.trim());
                    dto.setContent(content.trim());
                    dto.setSource("조선비즈");
                    dto.setPublishDate(published);

                    newsList.add(dto);
                    log.info("[수집] {} (총 {}개)", title, newsList.size());

                } catch (TimeoutException te) {
                    log.warn("[Timeout] {}", link);
                    takeScreenshot(driver, "/tmp/timeout_" + System.currentTimeMillis() + ".png");
                } catch (Exception ex) {
                    log.warn("[단건 실패] {} - {}", link, ex.toString());
                    takeScreenshot(driver, "/tmp/error_" + System.currentTimeMillis() + ".png");
                }
            }

        } catch (Exception e) {
            log.error("[크롤링 전체 실패]", e);
        } finally {
            if (driver != null) {
                try { driver.quit(); } catch (Exception ignore) {}
            }
            // /dev/shm 임시 프로필/캐시 정리 (락/용량 이슈 방지)
            safeDeleteTree(profileDir);
            safeDeleteTree(cacheDir);
        }

        log.info("[최종] 크롤링된 뉴스 개수: {}", newsList.size());
        return newsList;
    }

    // ===== 유틸 =====

    private static String firstNonBlankText(WebDriver d, String... selectors) {
        for (String css : selectors) {
            List<WebElement> els = d.findElements(By.cssSelector(css));
            if (!els.isEmpty()) {
                String t = els.get(0).getText();
                if (!isBlank(t)) return t.trim();
            }
        }
        return null;
    }

    private static List<WebElement> firstNonEmptyElements(WebDriver d, String... selectors) {
        for (String css : selectors) {
            List<WebElement> els = d.findElements(By.cssSelector(css));
            if (!els.isEmpty()) return els;
        }
        return List.of();
    }

    private static String getMetaContent(WebDriver d, String css) {
        List<WebElement> els = d.findElements(By.cssSelector(css));
        if (!els.isEmpty()) {
            String v = els.get(0).getAttribute("content");
            if (!isBlank(v)) return v.trim();
        }
        return null;
    }

    private static void takeScreenshot(WebDriver driver, String path) {
        try {
            var file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(file.toPath(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            log.info("[스샷 저장] {}", path);
        } catch (Exception ignore) {}
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static void safeDeleteTree(Path dir) {
        if (dir == null) return;
        try {
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignore) {} });
            }
        } catch (Exception ignore) {}
    }
}
