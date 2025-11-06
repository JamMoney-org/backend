package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FinanceNewsCrawler {

    // 메인은 JS 렌더 → 공식 Arc RSS를 씨드로 사용
    private static final List<String> RSS_URLS = List.of(
            "https://www.chosun.com/arc/outboundfeeds/rss/category/economy/?outputType=xml", // 경제
            "https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml"                  // 전체
    );

    private static final int TIMEOUT_MS = 10_000;
    private static final int MAX_ITEMS  = 3;

    public List<NewsRequestDto> fetchTodayNews() {
        log.info("[크롤링 시작]");
        List<NewsRequestDto> newsList = new ArrayList<>();

        try {
            // 1) RSS에서 최신 기사 링크 수집 (메인 목록 대체)
            List<String> links = fetchLinksFromRss(MAX_ITEMS);
            log.info("[RSS] 수집된 링크 수: {}", links.size());

            // 2) 상세 페이지 파싱 (제목/본문/발행일)
            for (int idx = 0; idx < links.size() && newsList.size() < MAX_ITEMS; idx++) {
                String link = links.get(idx);
                log.info("[{}] 상세 페이지 이동 → {}", idx, link);

                try {
                    NewsRequestDto dto = parseArticle(link);
                    if (dto == null) {
                        log.warn("[{}] 요소를 찾지 못해 스킵합니다.", idx);
                        continue;
                    }
                    newsList.add(dto);
                    log.info("[{}] 크롤링 완료 항목 수: {}", idx, newsList.size());
                    Thread.sleep(400); // 예의상 간격
                } catch (Exception e) {
                    log.warn("[{}] 단건 실패 스킵: {} - {}", idx, link, e.toString());
                }
            }
        } catch (Exception e) {
            log.error("[크롤링 전체 실패]", e);
        }

        log.info("[최종] 크롤링된 뉴스 개수: {}", newsList.size());
        return newsList;
    }

    /** RSS에서 최신 링크 수집 (Arc RSS 호환: RSS2/Atom 모두 처리). */
    private List<String> fetchLinksFromRss(int limit) {
        List<String> acc = new ArrayList<>();
        for (String rss : RSS_URLS) {
            try {
                Connection conn = connectForRss(rss);
                Connection.Response res = conn.execute(); // 상태코드 확인
                int sc = res.statusCode();
                if (sc < 200 || sc >= 300) {
                    log.warn("[RSS HTTP {}] {}", sc, rss);
                    continue;
                }
                Document doc = res.parse();

                // RSS2: <item><link>
                doc.select("item > link").stream()
                        .map(Element::text)
                        .filter(u -> u != null && u.startsWith("http"))
                        .forEach(acc::add);

                // Atom: <entry><link href="...">
                doc.select("entry > link[href]").stream()
                        .map(e -> e.attr("href"))
                        .filter(u -> u != null && u.startsWith("http"))
                        .forEach(acc::add);

            } catch (Exception e) {
                log.warn("[RSS 실패] {} - {}", rss, e.toString());
            }
            if (acc.size() >= limit) break;
        }

        // 원하는 섹션만 필터링 → 중복 제거 → 상한
        return acc.stream()
                .filter(u -> u.contains("biz.chosun.com"))
                .filter(u -> u.contains("/stock/finance/") || u.contains("/finance/"))
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /** 상세 페이지에서 제목/본문/발행일을 파싱해 DTO로 만든다. */
    private NewsRequestDto parseArticle(String url) throws Exception {
        Document doc = connectHtml(url).get();

        // 제목: 사이트 전용 셀렉터 우선 → og:title 백업
        String title = optText(doc, "h1.article-header__headline")
                .orElseGet(() -> meta(doc, "meta[property=og:title]").orElse(null));
        if (blank(title)) return null;

        // 본문: 기존 CSS 기준 유지
        String content = doc.select("p.article-body__content, div.article-body p, article p, div[itemprop=articleBody] p")
                .stream()
                .map(Element::text)
                .filter(s -> !blank(s))
                .collect(Collectors.joining("\n\n"));
        if (blank(content)) return null;

        // 발행일: 메타 → 수정일 → (백업) 오늘
        LocalDate published = LocalDate.now();
        String iso = meta(doc, "meta[property=article:published_time]")
                .orElseGet(() -> meta(doc, "meta[property=article:modified_time]").orElse(null));
        if (!blank(iso)) {
            try { published = OffsetDateTime.parse(iso).toLocalDate(); } catch (Exception ignore) {}
        }

        NewsRequestDto dto = new NewsRequestDto();
        dto.setTitle(title.trim());
        dto.setContent(content.trim());
        dto.setSource("조선비즈");
        dto.setPublishDate(published);
        return dto;
    }

    /** RSS 요청용 커넥션(헤더 최적화). */
    private static Connection connectForRss(String url) {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Safari")
                .referrer("https://www.google.com")
                .header("Accept", "application/rss+xml, application/xml;q=0.9, */*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9")
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .maxBodySize(0);
    }

    /** 기사 HTML 요청용 커넥션. */
    private static Connection connectHtml(String url) {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Safari")
                .referrer("https://www.google.com")
                .header("Accept-Language", "ko-KR,ko;q=0.9")
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .maxBodySize(0);
    }

    private static Optional<String> optText(Document d, String css) {
        Element e = d.selectFirst(css);
        return Optional.ofNullable(e == null ? null : e.text()).filter(s -> !blank(s));
    }
    private static Optional<String> meta(Document d, String css) {
        Element e = d.selectFirst(css);
        return Optional.ofNullable(e == null ? null : e.attr("content")).filter(s -> !blank(s));
    }
    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
}
