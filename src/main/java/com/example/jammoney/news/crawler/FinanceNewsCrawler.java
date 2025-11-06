package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FinanceNewsCrawler {

    private static final String BASE_URL = "https://biz.chosun.com/finance/";
    private static final int TIMEOUT_MS = 8000;
    private static final int MAX_ITEMS = 3;

    public List<NewsRequestDto> fetchTodayNews() {
        log.info("[크롤링 시작]");
        List<NewsRequestDto> result = new ArrayList<>();

        try {
            Document mainDoc = Jsoup.connect(BASE_URL)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Safari")
                    .timeout(TIMEOUT_MS)
                    .get();

            // 메인에서 기사 링크 수집
            List<String> links = mainDoc.select("a.story-card__headline")
                    .stream()
                    .map(e -> e.attr("href"))
                    .filter(href -> href.startsWith("http"))
                    .distinct()
                    .collect(Collectors.toList());

            log.info("[메인] 링크 수: {}", links.size());

            for (String link : links) {
                if (result.size() >= MAX_ITEMS) break;

                try {
                    NewsRequestDto dto = parseArticle(link);
                    if (dto != null) {
                        result.add(dto);
                        log.info("[수집 완료] {} (총 {}개)", dto.getTitle(), result.size());
                    } else {
                        log.warn("[스킵] 본문 비어있음 {}", link);
                    }
                } catch (IOException e) {
                    log.warn("[스킵] 연결 실패 {} - {}", link, e.toString());
                } catch (Exception e) {
                    log.warn("[스킵] 파싱 실패 {} - {}", link, e.toString());
                }
            }

        } catch (IOException e) {
            log.error("[메인 페이지 크롤링 실패]", e);
        }

        log.info("[최종] 크롤링된 뉴스 개수: {}", result.size());
        return result;
    }

    private NewsRequestDto parseArticle(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Safari")
                .timeout(TIMEOUT_MS)
                .get();

        String title = Optional.ofNullable(doc.selectFirst("h1.article-header__headline"))
                .map(Element::text)
                .orElseGet(() ->
                        Optional.ofNullable(doc.selectFirst("meta[property=og:title]"))
                                .map(e -> e.attr("content"))
                                .orElse(null)
                );

        if (title == null || title.isBlank()) return null;

        String content = doc.select("p.article-body__content, div.article-body p, article p, div[itemprop=articleBody] p")
                .stream()
                .map(Element::text)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("\n\n"));

        if (content.isBlank()) return null;

        // 발행일 (메타 or 오늘)
        LocalDate published = LocalDate.now();
        String iso = Optional.ofNullable(doc.selectFirst("meta[property=article:published_time]"))
                .map(e -> e.attr("content"))
                .orElse(null);

        if (iso != null && !iso.isBlank()) {
            try {
                published = OffsetDateTime.parse(iso).toLocalDate();
            } catch (Exception ignore) {}
        }

        NewsRequestDto dto = new NewsRequestDto();
        dto.setTitle(title.trim());
        dto.setContent(content.trim());
        dto.setSource("조선비즈");
        dto.setPublishDate(published);
        return dto;
    }
}
