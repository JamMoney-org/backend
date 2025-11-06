package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FinanceNewsCrawler {

    // ====== 셀렉터 (1순위 / 폴백) ======
    private static final By SEL_MAIN_LINKS            = By.cssSelector("a.story-card__headline");

    private static final By SEL_DETAIL_TITLE          = By.cssSelector("h1.article-header__headline");
    private static final By SEL_DETAIL_TITLE_FALLBACK = By.cssSelector("h1.article-title, h1#news_title, header h1");

    private static final By SEL_DETAIL_PARAS          = By.cssSelector("p.article-body__content");
    private static final By SEL_DETAIL_PARAS_FALLBACK = By.cssSelector("div.article-body p, article p, .article-content p");

    // ====== 정책/타임아웃 ======
    private static final int      NEED_SUCCESS      = 3;                  // 최종 수집 목표 개수
    private static final int      MAX_TRIES         = 12;                 // 최대 시도 링크 수(조금 여유)
    private static final int      MIN_PARAS         = 3;                  // 본문 최소 문단
    private static final int      MAX_PARAS         = 30;                 // 본문 최대 문단
    private static final long     BETWEEN_TRIES_MS  = 500;                // 링크 간 휴지
    private static final Duration MAIN_WAIT         = Duration.ofSeconds(20);
    private static final Duration DETAIL_WAIT       = Duration.ofSeconds(10); // 각 요소 대기
    private static final Duration PAGELOAD_TIMEOUT  = Duration.ofSeconds(15);
    private static final Duration SCRIPT_TIMEOUT    = Duration.ofSeconds(10);

    private static final String   UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/120 Safari/537.36";

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
            driver = newDriver(); // 옵션/전략/타임아웃 세팅된 새 드라이버

            // ====== 메인 페이지 ======
            driver.get("https://biz.chosun.com/finance/");
            WebDriverWait waitMain = new WebDriverWait(driver, MAIN_WAIT);
            waitMain.until(ExpectedConditions.presenceOfAllElementsLocatedBy(SEL_MAIN_LINKS));

            List<String> links = driver.findElements(SEL_MAIN_LINKS).stream()
                    .map(e -> safeAttr(e, "href"))
                    .filter(h -> h != null && h.startsWith("http"))
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));

            log.info("[메인] 링크 수(초기): {}", links.size());

            // 기사형 URL 필터(사이트 패턴에 맞춰 보수적으로 걸러서 실패 확률↓)
            links = links.stream()
                    .filter(u -> u.contains("/stock/finance/") || u.contains("/finance/")
                            || u.matches(".*/\\d{4}/\\d{2}/\\d{2}/.*"))
                    .collect(Collectors.toCollection(ArrayList::new));
            log.info("[메인] 링크 수(필터 후): {}", links.size());

            int ok = 0, tries = 0, consecutiveTimeouts = 0;

            for (int idx = 0; idx < links.size() && tries < MAX_TRIES && ok < NEED_SUCCESS; idx++) {
                String link = links.get(idx);
                tries++;
                log.info("[{}] 이동 → {}", idx, link);

                try {
                    NewsRequestDto dto = crawlOne(driver, link);
                    if (dto != null) {
                        newsList.add(dto);
                        ok++;
                        consecutiveTimeouts = 0;
                        log.info("[{}] 수집 성공 (성공: {}/{}, 시도: {}/{})",
                                idx, ok, NEED_SUCCESS, tries, MAX_TRIES);
                    } else {
                        log.warn("[{}] 컨텐츠 없음 → 스킵", idx);
                    }
                } catch (TimeoutException te) {
                    log.warn("[{}] ⏱️ Timeout → 스킵: {}", idx, te.getMessage());
                    consecutiveTimeouts++;
                    // 연속 타임아웃 2회 이상 → 드라이버 재시작으로 복구 시도
                    if (consecutiveTimeouts >= 2) {
                        log.warn("[복구] 연속 타임아웃 {}회 → 드라이버 재시작", consecutiveTimeouts);
                        quitHard(driver);
                        driver = newDriver();
                        consecutiveTimeouts = 0;
                    }
                } catch (NoSuchSessionException nse) {
                    log.warn("[{}] 세션 깨짐 → 드라이버 재시작: {}", idx, nse.getMessage());
                    quitHard(driver);
                    driver = newDriver();
                } catch (Exception ex) {
                    log.warn("[{}] 예외 → 스킵: {}: {}", idx, ex.getClass().getSimpleName(), ex.getMessage());
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

    // ====== 단일 링크 처리: 원본 → 실패 시 AMP(JSoup) 폴백 ======
    private NewsRequestDto crawlOne(WebDriver driver, String url) throws Exception {
        // 1) 원본(요소만 짧게 대기)
        try {
            driver.navigate().to(url);

            // 진입 직후 오버레이/동의창 한 번 닫아보기(실패해도 무시)
            dismissOverlays(driver);

            WebDriverWait waitDetail = new WebDriverWait(driver, DETAIL_WAIT);

            String title = firstNonEmptyText(driver, waitDetail, SEL_DETAIL_TITLE, SEL_DETAIL_TITLE_FALLBACK);
            if (title.isBlank()) throw new TimeoutException("title not found");

            // 본문 최소 3문단 등장까지 대기
            waitDetail.until(minCount(SEL_DETAIL_PARAS, Math.min(3, MIN_PARAS)));
            String content = joinTexts(driver.findElements(SEL_DETAIL_PARAS));

            if (content.isBlank() || countNonBlank(driver.findElements(SEL_DETAIL_PARAS)) < MIN_PARAS) {
                // 폴백 셀렉터 재시도
                waitDetail.until(minCount(SEL_DETAIL_PARAS_FALLBACK, 2));
                content = joinTexts(driver.findElements(SEL_DETAIL_PARAS_FALLBACK));
            }

            if (content.length() < 80) throw new TimeoutException("body too short");

            return makeDto(title, content, "조선비즈");
        } catch (TimeoutException | NoSuchElementException e) {
            log.warn("[원본 실패] {} → AMP 폴백: {}", e.getClass().getSimpleName(), url);
        }

        // 2) AMP(JSoup) 폴백
        try {
            return crawlAmpWithJsoup(url);
        } catch (Exception ampEx) {
            log.warn("[AMP 폴백 실패] {}: {}", ampEx.getClass().getSimpleName(), ampEx.getMessage());
            return null;
        }
    }

    // ====== AMP를 Jsoup로 파싱 (Selenium 부담 제로) ======
    private NewsRequestDto crawlAmpWithJsoup(String originalUrl) throws Exception {
        String ampUrl = toAmpUrl(originalUrl);
        if (ampUrl == null) throw new IllegalArgumentException("amp url not derivable");

        Document doc = Jsoup.connect(ampUrl)
                .userAgent(UA)
                .referrer("https://www.google.com/")
                .timeout(8000) // 8초
                .get();

        String title = firstText(doc.selectFirst("h1"));
        if (title.isBlank()) title = firstText(doc.selectFirst("header h1"));

        List<String> lines = new ArrayList<>();
        doc.select("article p, main p, .article-body p, .article-content p")
                .stream()
                .map(el -> el.text() == null ? "" : el.text().trim())
                .filter(s -> !s.isBlank())
                .limit(MAX_PARAS)
                .forEach(lines::add);

        if (lines.size() < MIN_PARAS) throw new IllegalStateException("AMP body too short");

        String content = String.join("\n\n", lines);
        return makeDto(title, content, "조선비즈(AMP)");
    }

    private static String toAmpUrl(String url) {
        if (url == null) return null;
        if (url.contains("biz.chosun.com") && !url.contains("/amp/")) {
            return url.replace("://biz.chosun.com/", "://biz.chosun.com/amp/");
        }
        return url;
    }

    // ====== WebDriver 생성 공통(옵션/전략/타임아웃 세팅 일원화) ======
    private WebDriver newDriver() {
        ChromeOptions options = new ChromeOptions();

        // 바이너리 경로(환경변수 우선, 없으면 흔한 경로 탐색)
        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.isBlank()) {
            options.setBinary(chromeBin);
            log.info("[Chrome 바이너리] {}", chromeBin);
        } else {
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

        // 전체 페이지 로드 기다리지 않기(EAGER) + 리소스 절감 플래그
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--incognito",
                "--lang=ko-KR",
                "--user-agent=" + UA,

                "--user-data-dir=/tmp/chrome-profile",
                "--disk-cache-dir=/tmp/chrome-cache",
                "--disk-cache-size=0",
                "--media-cache-size=0",

                "--disable-gpu",
                "--disable-software-rasterizer",
                "--use-angle=swiftshader",
                "--use-gl=swiftshader",

                "--blink-settings=imagesEnabled=false",
                "--disable-plugins",
                "--disable-background-networking",
                "--disable-domain-reliability",
                "--disable-sync",
                "--disable-features=Translate,BackForwardCache",
                "--no-first-run",
                "--no-default-browser-check",

                "--disable-crash-reporter",
                "--disable-logging",
                "--log-level=3",

                "--window-size=1280,800"
        );

        ChromeDriver d = new ChromeDriver(options);
        d.manage().timeouts().implicitlyWait(Duration.ZERO);
        d.manage().timeouts().pageLoadTimeout(PAGELOAD_TIMEOUT);
        d.manage().timeouts().scriptTimeout(SCRIPT_TIMEOUT);

        log.info("[PLS: EAGER] pageLoadTimeout={} / scriptTimeout={}",
                PAGELOAD_TIMEOUT, SCRIPT_TIMEOUT);

        return d;
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

    /** 진입 직후 흔한 오버레이/닫기 버튼을 한 번 눌러본다(실패해도 무시) */
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

    private static String firstText(org.jsoup.nodes.Element el) {
        return (el == null) ? "" : el.text();
    }

    private static NewsRequestDto makeDto(String title, String content, String source) {
        NewsRequestDto dto = new NewsRequestDto();
        dto.setTitle(title == null ? "" : title.trim());
        dto.setContent(content == null ? "" : content.trim());
        dto.setSource(source);
        dto.setPublishDate(LocalDate.now());
        return dto;
    }

    /** 정상 종료(잔여 윈도우/쿠키 정리) */
    private static void safeQuit(WebDriver driver) {
        if (driver == null) return;
        try { driver.manage().deleteAllCookies(); } catch (Exception ignore) {}
        try {
            for (String h : driver.getWindowHandles()) {
                try { driver.switchTo().window(h).close(); } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}
        try { driver.quit(); } catch (Exception ignore) {}
    }

    /** quit 타임아웃 시 크롬/드라이버 강제 종료 */
    private static void quitHard(WebDriver d) {
        try { safeQuit(d); } catch (Exception ignore) {}
        try {
            new ProcessBuilder("sh", "-lc",
                    "pkill -f 'chromium|google-chrome|chrome|chromedriver' || true"
            ).start().waitFor();
        } catch (Exception ignore) {}
    }
}
