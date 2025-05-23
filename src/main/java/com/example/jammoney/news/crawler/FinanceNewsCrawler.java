
package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
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

    public List<NewsRequestDto> fetchTodayNews() {
        log.info("[크롤링 시작]");
        WebDriver driver = null;
        List<NewsRequestDto> newsList = new ArrayList<>();

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            driver = new ChromeDriver(options);
            driver.get("https://biz.chosun.com/finance/");

            String mainPageHtml = driver.getPageSource(); // HTML 확인용
            log.info("[메인 페이지 HTML 일부 출력] \n{}", mainPageHtml.substring(0, Math.min(1000, mainPageHtml.length())));
            List<WebElement> elements = driver.findElements(By.cssSelector("a.text_link.story-card__headline"));


// 더 유연한 셀렉터로 교체
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.story-card__headline")));

            List<WebElement> elementList = driver.findElements(By.cssSelector("a.story-card__headline"));
            System.out.println("[검색된 요소 개수] " + elementList.size());

/*            for (int i = 0; i < elementList.size(); i++) {
                WebElement el = elementList.get(i);
                String text = el.getText();
                String href = el.getAttribute("href");
                System.out.println("[" + i + "] 제목: " + text);
                System.out.println("[" + i + "] 링크: https://biz.chosun.com" + href); // 상대경로 보정
                System.out.println("----------");
                driver.get("https://biz.chosun.com" + href);
                String mainPageHtml_2 = driver.getPageSource(); // HTML 확인용
                log.info("[메인 페이지 HTML 일부 출력] \n{}", mainPageHtml_2.substring(0, Math.min(1000, mainPageHtml_2.length())));
            }*/
            for (int i = 0; i < elementList.size(); i++) {
                WebElement el = elementList.get(i);
                String text = el.getText();
                String href = el.getAttribute("href");

                // 상대경로 → 절대경로 변환
                String fullUrl = "https://biz.chosun.com/finance/" + href;

                System.out.println("[" + i + "] 제목: " + text);
                System.out.println("[" + i + "] 링크: " + fullUrl);
                System.out.println("----------");

                // 상세 페이지 이동
                driver.get(fullUrl);

                // HTML 일부 출력
                String mainPageHtml_2 = driver.getPageSource();
                log.info("[상세 페이지 HTML 일부 출력] \n{}", mainPageHtml_2.substring(0, Math.min(1000, mainPageHtml_2.length())));

                List<WebElement> elementsss = driver.findElements(By.cssSelector("h1.article-header__headline"));
                WebElement el1 = elementList.get(i);
                String textt = el1.getText();
                System.out.println("[" + i + "] ㅈㅂ나와야되는 제목: " + textt);
            }


            int count = Math.min(3, elementList.size());

            for (int i = 0; i < count; i++) {
                String link = elementList.get(i).getAttribute("href");
                log.info("상세 페이지 이동: {}", link);
                driver.navigate().to(link);

                WebDriverWait detailWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                WebElement titleEl = detailWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1.article-header__headline")));
                WebElement contentEl = driver.findElement(By.cssSelector("div.article-body__content"));

                String title = titleEl.getText();
                String content = contentEl.getText();

                NewsRequestDto dto = new NewsRequestDto();
                dto.setTitle(title);
                dto.setContent(content);
                dto.setSource("조선비즈");
                dto.setPublishDate(LocalDate.now());

                newsList.add(dto);

                driver.navigate().back();
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            log.error("[크롤링 전체 실패]", e);
        } finally {
            if (driver != null) driver.quit();
        }

        log.info("[크롤링된 뉴스 개수]: {}", newsList.size());
        return newsList;
    }
}
