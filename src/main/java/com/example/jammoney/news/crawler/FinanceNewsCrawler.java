package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

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
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        List<NewsRequestDto> result = new ArrayList<>();

        try {
            driver.get(URL);
            List<WebElement> newsElements = driver.findElements(By.cssSelector(".type06_headline li"));

            int count = 0;
            for (WebElement el : newsElements) {
                if (count >= 3) break;

                WebElement anchor = null;
                try {
                    anchor = el.findElement(By.cssSelector("dl > dt > a"));
                } catch (Exception ignore) {}

                if (anchor == null || anchor.getText().isBlank()) continue;

                String link = anchor.getAttribute("href");

                // 상세 페이지로 이동
                driver.get(link);
                Thread.sleep(1000); // 로딩 대기

                String title = "";
                String content = "";
                String source = "출처 미상";

                try {
                    WebElement titleEl = driver.findElement(By.cssSelector("h2#title_area.media_end_head_headline > span"));
                    title = titleEl.getText();
                    log.info("title: "+title);

                    WebElement contentEl = driver.findElement(By.id("dic_area"));
                    content = contentEl.getText();

                    WebElement sourceEl = driver.findElement(By.cssSelector(".media_end_head_top_logo img"));
                    source = sourceEl.getAttribute("alt");
                } catch (Exception e) {
                    System.out.println("[경고] 크롤링 실패: " + link);
                    continue; // 다음 뉴스로
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

                driver.navigate().back();
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("크롤링 중 에러 발생", e);
        } finally {
            driver.quit();
        }

        return result;
    }
}
