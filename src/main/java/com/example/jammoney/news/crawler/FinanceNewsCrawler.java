package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
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
        System.setProperty("webdriver.chrome.driver",
                "C:\\Users\\gahyeon\\Desktop\\chromedriver-win64\\chromedriver.exe");
        log.info("[ChromeDriver 경로 설정 완료]");
    }

    public List<NewsRequestDto> fetchTodayNews() {
        log.info("[크롤링 시작]");
        List<NewsRequestDto> newsList = new ArrayList<>();
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new ChromeDriver(options);

            // 1) 메인 페이지 접속 & 리스트 로딩 대기
            driver.get("https://biz.chosun.com/finance/");
            WebDriverWait waitMain = new WebDriverWait(driver, Duration.ofSeconds(10));
            waitMain.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("a.story-card__headline")));

            // 2) 모든 링크 미리 수집
            List<WebElement> cards = driver.findElements(By.cssSelector("a.story-card__headline"));
            log.info("[메인] 검색된 기사 개수: {}", cards.size());

            List<String> links = new ArrayList<>();
            for (WebElement card : cards) {
                String href = card.getAttribute("href");
                if (href != null && href.startsWith("http")) {
                    links.add(href);
                }
            }

            // 3) 하루에 3개만, 스킵된 기사 건너뛰기
            for (int idx = 0; idx < links.size() && newsList.size() < 3; idx++) {
                String link = links.get(idx);
                log.info("[{}] 상세 페이지 이동 → {}", idx, link);
                driver.navigate().to(link);

                WebDriverWait waitDetail = new WebDriverWait(driver, Duration.ofSeconds(5));
                try {
                    // 제목
                    WebElement titleEl = waitDetail.until(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.cssSelector("h1.article-header__headline")));
                    String title = titleEl.getText();
                    log.info("[{}] 제목: {}", idx, title);

                    // 본문: 여러 <p.article-body__content> 태그 모두 합치기
                    List<WebElement> paras = waitDetail.until(
                            ExpectedConditions.presenceOfAllElementsLocatedBy(
                                    By.cssSelector("p.article-body__content")));
                    StringBuilder contentBuilder = new StringBuilder();
                    for (WebElement p : paras) {
                        contentBuilder.append(p.getText().trim()).append("\n\n");
                    }
                    String content = contentBuilder.toString().trim();
                    log.info("[{}] 본문 문단 개수: {}, 총 길이: {}자",
                            idx, paras.size(), content.length());

                    // (기존대로) 출처, 발간일
                    String source = "조선비즈";
                    LocalDate publishDate = LocalDate.now();

                    // DTO 생성 & 저장
                    NewsRequestDto dto = new NewsRequestDto();
                    dto.setTitle(title);
                    dto.setContent(content);
                    dto.setSource(source);
                    dto.setPublishDate(publishDate);
                    newsList.add(dto);
                    log.info("[{}] 크롤링 완료 항목 수: {}", idx, newsList.size());

                } catch (TimeoutException te) {
                    log.warn("[{}] 본문 또는 제목 요소를 찾지 못해 스킵합니다.", idx);
                } catch (Exception ex) {
                    log.error("[{}] 크롤링 중 예기치 못한 오류 발생", idx, ex);
                }

                Thread.sleep(1000);  // 서버 부하 방지
            }

        } catch (Exception e) {
            log.error("[크롤링 전체 실패]", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        log.info("[최종] 크롤링된 뉴스 개수: {}", newsList.size());
        return newsList;
    }
}
