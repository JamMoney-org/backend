package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

    private static final String DRIVER_PATH = "C:/Users/gahyeon/Desktop/chromedriver-win64/chromedriver.exe";
    private static final String URL = "https://finance.naver.com/news/news_list.naver?mode=LSS2D&section_id=101&section_id2=258";

    public List<NewsRequestDto> fetchTodayNews() {
        System.setProperty("webdriver.chrome.driver", DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new"); // 디버깅용으로 주석 처리 (크롬 창 뜨게)
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        List<NewsRequestDto> result = new ArrayList<>();

        try {
            System.out.println("=== 크롤링 시작 ===");
            driver.get(URL);
            System.out.println("뉴스 리스트 페이지 접속 완료");

            // ✅ 요소 로딩 기다리기 (최대 5초)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".news_list li")));

            List<WebElement> newsElements = driver.findElements(By.cssSelector(".news_list li"));
            System.out.println("가져온 뉴스 리스트 개수: " + newsElements.size());

            // 링크 먼저 수집
            List<String> links = new ArrayList<>();
            for (WebElement el : newsElements) {
                try {
                    WebElement anchor = el.findElement(By.cssSelector("a"));
                    String href = anchor.getAttribute("href");
                    System.out.println("링크 추출 성공: " + href);
                    if (!href.isBlank()) {
                        links.add(href);
                    }
                } catch (Exception e) {
                    System.out.println("링크 추출 실패: " + e.getMessage());
                }
                if (links.size() >= 3) break;
            }

            System.out.println("최종 수집된 뉴스 링크 수: " + links.size());

            // 상세 페이지 크롤링
            int count = 0;
            for (String link : links) {
                driver.get(link);
                Thread.sleep(1000); // 로딩 대기

                String title = "";
                String content = "";
                String source = "출처 미상";

                try {
                    WebElement titleEl = driver.findElement(By.cssSelector("h2#title_area.media_end_head_headline"));
                    title = titleEl.getText();

                    WebElement contentEl = driver.findElement(By.id("dic_area"));
                    content = contentEl.getText();

                    try {
                        WebElement sourceEl = driver.findElement(By.cssSelector(".media_end_head_top_logo_text"));
                        source = sourceEl.getText();
                    } catch (Exception ignored) {
                        try {
                            WebElement altEl = driver.findElement(By.cssSelector(".media_end_head_top_logo img"));
                            source = altEl.getAttribute("alt");
                        } catch (Exception ignoredAgain) {}
                    }

                    NewsRequestDto dto = new NewsRequestDto();
                    dto.setTitle(title);
                    dto.setPublishDate(LocalDate.now());
                    dto.setSource(source);
                    dto.setContent(content);
                    result.add(dto);
                    count++;

                    System.out.println("[" + count + "] 제목: " + title);
                    System.out.println("     출처: " + source);
                    System.out.println("     내용: " + (content.length() > 50 ? content.substring(0, 50) + "..." : content));
                    System.out.println("     URL: " + link);
                    System.out.println("-------------------------------------------------");

                } catch (Exception e) {
                    System.out.println("[경고] 뉴스 상세 크롤링 실패: " + link);
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println("[전체 크롤링 실패]");
            e.printStackTrace();
            throw new RuntimeException("크롤링 중 에러 발생", e);
        } finally {
            driver.quit();
        }

        return result;
    }
}
