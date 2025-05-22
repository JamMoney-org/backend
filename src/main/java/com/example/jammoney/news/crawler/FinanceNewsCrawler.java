package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class FinanceNewsCrawler {

    private static final String DRIVER_PATH = "C:/Users/gahyeon/Desktop/chromedriver-win64/chromedriver.exe";
    private static final String URL = "https://finance.naver.com/news/news_list.naver?mode=LSS2D&section_id=101&section_id2=258";

    public List<NewsRequestDto> fetchTodayNews() {
        System.setProperty("webdriver.chrome.driver", DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // UI 없이 실행
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        List<NewsRequestDto> result = new ArrayList<>();

        try {
            driver.get(URL);
            List<WebElement> newsElements = driver.findElements(By.cssSelector(".type06 li"));

            int count = 0;
            for (WebElement el : newsElements) {
                if (count >= 3) break;

                WebElement anchor = null;
                try {
                    anchor = el.findElement(By.cssSelector("dt > a"));
                } catch (Exception ignore) {}

                if (anchor == null || anchor.getText().isBlank()) continue;

                String title = anchor.getText();
                String link = anchor.getAttribute("href");

                // 상세 페이지로 이동
                driver.get(link);
                Thread.sleep(1000); // 로딩 대기

                String content = "";
                try {
                    WebElement contentEl = driver.findElement(By.id("news_read"));
                    content = contentEl.getText();
                } catch (Exception e) {
                    content = "(본문을 불러올 수 없습니다)";
                }

                String source = "출처 미상";
                try {
                    source = el.findElement(By.className("writing")).getText();
                } catch (Exception ignore) {}

                NewsRequestDto dto = new NewsRequestDto();
                dto.setTitle(title);
                dto.setSource(source);
                dto.setPublishDate(LocalDate.now());
                dto.setContent(content);
                result.add(dto);
                count++;

                System.out.println("뉴스 제목: " + title);
                System.out.println("뉴스 링크: " + link);
                System.out.println("뉴스 내용 (요약): " + (content.length() > 50 ? content.substring(0, 50) + "..." : content));

                driver.navigate().back(); // 목록 페이지로 돌아가기
                Thread.sleep(1000); // 다시 로딩 대기
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
