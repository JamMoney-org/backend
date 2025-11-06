package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FinanceNewsCrawler {

    // ====== 셀렉터 (1순위 / 폴백) ======
    private static final By SEL_MAIN_LINKS           = By.cssSelector("a.story-card__headline");

    private static final By SEL_DETAIL_TITLE         = By.cssSelector("h1.article-header__headline");
    private static final By SEL_DETAIL_TITLE_FALLBACK= By.cssSelector("h1.article-title, h1#news_title, header h1");

    private static final By SEL_DETAIL_PARAS         = By.cssSelector("p.article-body__content");
    private static final By SEL_DETAIL_PARAS_FALLBACK= By.cssSelector("div.article-body p, article p, .article-content p");

    // ====== 정책/타임아웃 ======
    private static final int    NEED_SUCCESS   = 3;                 // 필요 개수
    private static final int    MAX_TRIES      = 8;                 // 최대 시도 링크 수
    private static final int    MIN_PARAS      = 3;                 // 본문 최소 문단(완화)
    private static final int    MAX_PARAS      = 30;                // 본문 최대 문단
    private static final long   BETWEEN_TRIES_MS = 500;             // 링크 간 휴지
    private static final Duration MAIN_WAIT    = Duration.ofSeconds(20);
    private static final Duration DETAIL_WAIT  = Duration.ofSeconds(12);

    // ====== 드라이버 경로 세팅 ======
    @PostConstruct
    public void initChromeDriverPath() {
        String driverPath = System.getenv("CHROMEDRIVER_BIN");
        if (driverPath != null && !driverPath.isBlank()) {
            System.setProperty("webdriver.chrome.driver", driverPath);
            log.info("[ChromeDriver 경로 설정] {}", driverPath);
        } else {
            WebDriverManager.chromedriver().setup();
            log.info("[WebDriverManager] ChromeDriver 자동 설치");
        }
    }

    public List<NewsRequestDto> fetchTodayNews() {
        log.info("[크롤링 시작]");
        List<NewsRequestDto> newsList = new ArrayList<>();
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();

            // Chromium 바이너리 경로
            String chromeBin = System.getenv("CHROME_BIN");
            if (chromeBin != null && !chromeBin.isBlank()) {
                options.setBinary(chromeBin);
                log.info("[Chrome 바이너리] {}", chromeBin);
            } else {
                // Alpine/Ubuntu 컨테이너 호환 경로들
                for (String cand : List.of("/usr/bin/chromium", "/usr/bin/chromium-browser", "/usr/bin/google-chrome")) {
                    try {
                        if (new java.io.File(cand).exists()) {
                            options.setBinary(cand);
                            log.info("[Chrome 바이너리 fallback] {}", cand);
                            break;
                        }
                    } catch (Exception ignore) {}
                }
            }

            // 안정화 + I/O 최소화
            options.addArguments(
                    "--headless=new",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--incognito",
                    "--lang=ko-KR",
                    "--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36",

                    // 디스크 쓰기를 tmpfs(/tmp)로 우회
                    "--user-data-dir=/tmp/chrome-profile",
                    "--disk-cache-dir=/tmp/chrome-cache",
                    "--disk-cache-size=0",
                    "--media-cache-size=0",

                    // 불필요 네트워크/기능 최소화
                    "--blink-settings=imagesEnabled=false",
                    "--disable-plugins",
                    "--disable-background-networking",
                    "--disable-domain-reliability",
                    "--disable-sync",
                    "--disable-features=Translate,BackForwardCache",
                    "--no-first-run",
                    "--no-default-browser-check",

                    // 로깅/크래시 레포트 축소
                    "--disable-crash-reporter",
                    "--disable-logging",
                    "--log-level=3",

                    // 기타
                    "--window-size=1280,800"
                    // "--no-zygote" // 필요시만
            );

            driver = new ChromeDriver(options);

            // ====== 메인 페이지 ======
            driver.get("https://biz.chosun.com/finance/");
            WebDriverWait waitMain = new WebDriverWait(driver, MAIN_WAIT);
            waitMain.until(ExpectedConditions.presenceOfAllElementsLocatedBy(SEL_MAIN_LINKS));

            List<String> links = driver.findElements(SEL_MAIN_LINKS).stream()
                    .map(e -> safeAttr(e, "href"))
                    .filter(h -> h != null && h.startsWith("http"))
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));

            log.info("[메인] 링크 수: {}", links.size());

            // 필요하면 기사형 URL만 필터링(패턴은 사이트에 맞춰 조정)
            links = links.stream()
                    .filter(u -> u.contains("/finance/") || u.matches(".*/\\d{4}/\\d{2}/\\d{2}/.*"))
                    .collect(Collectors.toCollection(ArrayList::new));

            int ok = 0, tries = 0;
            for (int idx = 0; idx < links.size() && tries < MAX_TRIES && ok < NEED_SUCCESS; idx++) {
                String link = links.get(idx);
                tries++;
                log.info("[{}] 이동 → {}", idx, link);

                try {
                    driver.navigate().to(link);

                    // 오버레이/동의창 있을 수 있어 진입 직후 한 번 닫기 시도(실패해도 무시)
                    dismissOverlays(driver);

                    WebDriverWait waitDetail = new WebDriverWait(driver, DETAIL_WAIT);

                    // 제목(1차/폴백)
                    String title = firstNonEmptyText(
                            driver, waitDetail, SEL_DETAIL_TITLE, SEL_DETAIL_TITLE_FALLBACK);
                    if (title.isBlank()) {
                        log.warn("[{}] 제목 추출 실패 → 스킵", idx);
                        continue;
                    }

                    // 본문: p 개수 기준으로 대기(최소 3)
                    waitDetail.until(minCount(SEL_DETAIL_PARAS, Math.min(3, MIN_PARAS)));
                    String content = joinTexts(driver.findElements(SEL_DETAIL_PARAS));

                    if (content.isBlank() || countNonBlank(driver.findElements(SEL_DETAIL_PARAS)) < MIN_PARAS) {
                        // 폴백 셀렉터 재시도
                        waitDetail.until(minCount(SEL_DETAIL_PARAS_FALLBACK, 2));
                        content = joinTexts(driver.findElements(SEL_DETAIL_PARAS_FALLBACK));
                    }

                    // AMP 페이지 폴백
                    if (content.isBlank() || content.length() < 80) {
                        try {
                            String amp = ampVersion(link);
                            if (amp != null) {
                                log.info("[{}] AMP 폴백 시도 → {}", idx, amp);
                                driver.navigate().to(amp);
                                waitDetail.until(minCount(SEL_DETAIL_PARAS_FALLBACK, 2));
                                content = joinTexts(driver.findElements(SEL_DETAIL_PARAS_FALLBACK));
                            }
                        } catch (Exception ignore) {}
                    }

                    if (content.isBlank()) {
                        log.warn("[{}] 본문 비어 스킵 (title.len={})", idx, title.length());
                        continue;
                    }

                    NewsRequestDto dto = new NewsRequestDto();
                    dto.setTitle(title.trim());
                    dto.setContent(content.trim());
                    dto.setSource("조선비즈");
                    dto.setPublishDate(LocalDate.now());

                    newsList.add(dto);
                    ok++;
                    log.info("[{}] ✅ 수집 성공 (성공: {}/{}, 시도: {}/{})", idx, ok, NEED_SUCCESS, tries, MAX_TRIES);

                } catch (TimeoutException te) {
                    log.warn("[{}] ⏱️ Timeout → 스킵: {}", idx, te.getMessage());
                } catch (Exception ex) {
                    log.warn("[{}] ❌ 예외 → 스킵: {}: {}", idx, ex.getClass().getSimpleName(), ex.getMessage());
                }

                try { Thread.sleep(BETWEEN_TRIES_MS); } catch (InterruptedException ignore) {}
            }

        } catch (Exception e) {
            log.error("[크롤링 전체 실패]", e);
        } finally {
            safeQuit(driver);
        }

        log.info("[최종] 크롤링된 뉴스 개수: {}", newsList.size());
        return newsList;
    }

    // ====== 유틸 ======

    private static String safeAttr(WebElement e, String name) {
        try { return e.getAttribute(name); } catch (Exception ignore) { return null; }
    }

    private static String joinTexts(List<WebElement> elems) {
        return elems.stream()
                .map(e -> {
                    try { return e.getText(); } catch (Exception ignore) { return ""; }
                })
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isBlank())
                .limit(MAX_PARAS)
                .collect(Collectors.joining("\n\n"));
    }

    private static int countNonBlank(List<WebElement> elems) {
        int c = 0;
        for (WebElement e : elems) {
            try {
                String t = e.getText();
                if (t != null && !t.isBlank()) c++;
            } catch (Exception ignore) {}
        }
        return c;
    }

    /** 셀렉터 두 개를 순서대로 시도해 첫 비어있지 않은 텍스트 반환 */
    private static String firstNonEmptyText(WebDriver d, WebDriverWait wait, By primary, By fallback) {
        try {
            String t = wait.until(ExpectedConditions.presenceOfElementLocated(primary)).getText();
            if (t != null && !t.isBlank()) return t;
        } catch (Exception ignore) {}
        try {
            String t2 = wait.until(ExpectedConditions.presenceOfElementLocated(fallback)).getText();
            return t2 == null ? "" : t2;
        } catch (Exception ignore) {}
        return "";
    }

    /** 지정 셀렉터의 요소 개수가 N 이상이 될 때까지 대기하는 조건 */
    private static ExpectedCondition<Boolean> minCount(By sel, int n) {
        return d -> {
            if (d == null) return false;
            try {
                int size = d.findElements(sel).size();
                return size >= n;
            } catch (Exception e) {
                return false;
            }
        };
    }

    /** 간단 AMP 경로 추정 (사이트 구조에 맞게 조정 가능) */
    private static String ampVersion(String url) {
        try {
            // 예: https://biz.chosun.com/finance/...  →  https://biz.chosun.com/amp/finance/...
            if (url.contains("biz.chosun.com") && !url.contains("/amp/")) {
                return url.replace("https://biz.chosun.com/", "https://biz.chosun.com/amp/");
            }
        } catch (Exception ignore) {}
        return null;
    }

    /** 진입 직후에 흔한 오버레이/닫기 버튼을 한 번 눌러본다(실패해도 무시) */
    private static void dismissOverlays(WebDriver d) {
        List<By> candidates = List.of(
                By.cssSelector("button[aria-label='닫기']"),
                By.cssSelector(".btn-close, .close, .ad_close, .modal-close")
        );
        for (By by : candidates) {
            try {
                WebElement el = new WebDriverWait(d, Duration.ofSeconds(1))
                        .until(ExpectedConditions.elementToBeClickable(by));
                el.click();
                break;
            } catch (Exception ignore) {}
        }
    }

    /** 윈도우/프로세스 정리까지 안전 종료 */
    private static void safeQuit(WebDriver driver) {
        if (driver == null) return;
        try { driver.manage().deleteAllCookies(); } catch (Exception ignore) {}
        try {
            for (String h : driver.getWindowHandles()) {
                try { driver.switchTo().window(h).close(); } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}
        try { driver.quit(); } catch (Exception ignore) {}

        // 환경에 따라 잔여 프로세스가 남을 경우 정리(컨테이너에서 init:true면 보통 불필요)
        try {
            new ProcessBuilder("sh", "-lc",
                    "pkill -f 'chromium|google-chrome|chrome|chromedriver' || true"
            ).start().waitFor();
        } catch (Exception ignore) {}
    }
}
