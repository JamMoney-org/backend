package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class FinanceNewsCrawler {

    private static final String URL = "https://finance.naver.com/news/mainnews.naver";

    public List<NewsRequestDto> fetchTodayNews() {
        List<NewsRequestDto> result = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(URL).get();
            Elements newsList = doc.select(".newsList > li");

            int count = 0;
            for (Element el : newsList) {
                if (count >= 3) break;

                Element anchor = el.selectFirst("a");
                if (anchor == null) continue; // a 태그 없으면 스킵

                String title = anchor.text();
                String link = "https://finance.naver.com" + anchor.attr("href");

                Document contentDoc = Jsoup.connect(link).get();
                Element contentEl = contentDoc.selectFirst("#news_read");
                String content = (contentEl != null) ? contentEl.text() : "";

                NewsRequestDto dto = new NewsRequestDto();
                dto.setTitle(title);
                dto.setSource("네이버금융");
                dto.setPublishDate(LocalDate.now());
                dto.setContent(content);

                result.add(dto);
                count++;
            }

        } catch (IOException e) {
            e.printStackTrace(); // 로그로 남기거나 슬랙 연동 추천
        }

        return result;
    }
}
