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

    // Arc RSS 사용 (경제 + 전체)
    private static final List<String> RSS_URLS = List.of(
            "https://www.chosun.com/arc/outboundfeeds/rss/category/economy/?outputType=xml",
            "https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml"
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

    // RSS에서 최신 링크 수집 (RSS2/Atom 겸용) - 도메인 필터 제거, 경로 패턴만 사용
    private List<String> fetchLinksFromRss(int limit) {
        List<String> acc = new ArrayList<>();

        for (String rss : RSS_URLS) {
            try {
                var res = connectForRss(rss).execute(); // 상태코드 확인
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
            if (acc.size() >= limit * 3) break; // 여유 있게 모아두고 나중에 필터
        }

        // 🔎 경로 패턴으로만 필터링 (도메인 제한 X)
        // - 재무/증권/경제 카테고리 위주로 흔히 쓰이는 경로들
        var filtered = acc.stream()
                .filter(u ->
                        u.contains("/stock/finance/") ||
                                u.contains("/finance/") ||
                                u.contains("/economy/") ||
                                u.contains("/money/") ||
                                u.contains("/market/"))
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());

        // 디버깅용: 처음 몇 개만 샘플로 찍기
        if (filtered.isEmpty()) {
            log.warn("[RSS] 필터 후 0건 — 첫 5개 원본 링크 샘플: {}",
                    acc.stream().limit(5).collect(Collectors.toList()));
        } else {
            log.info("[RSS] 필터 후 {}건, 샘플: {}", filtered.size(),
                    filtered.stream().limit(3).collect(Collectors.toList()));
        }

        return filtered;
    }

    private NewsRequestDto parseArticle(String url) throws Exception {
        // 1) 일반 페이지 시도 (기존 셀렉터 우선)
        Document doc = connectHtml(url).get();

        String title = optText(doc, "h1.article-header__headline")
                .orElseGet(() -> optText(doc, "h1.article-title").orElse(null)); // 조선/비즈 일부 템플릿
        String content = doc.select("p.article-body__content, div.article-body p, article p, div[itemprop=articleBody] p")
                .stream().map(Element::text).filter(s -> !blank(s)).collect(Collectors.joining("\n\n"));
        LocalDate published = extractPublished(doc).orElse(LocalDate.now());

        if (!blank(title) && !blank(content)) {
            NewsRequestDto dto = new NewsRequestDto();
            dto.setTitle(title.trim());
            dto.setContent(content.trim());
            dto.setSource("조선비즈");
            dto.setPublishDate(published);
            return dto;
        }

        // 2) AMP 뷰로 재시도 (조선닷컴은 AMP가 안정적으로 제공됨)
        String ampUrl = url.contains("outputType=amp") ? url
                : url + (url.contains("?") ? "&" : "?") + "outputType=amp";

        Document amp = connectHtml(ampUrl).get();

        // AMP는 보통 그냥 <h1>에 제목이 들어간다
        String ampTitle = optText(amp, "h1")
                .orElseGet(() -> optText(amp, "header h1").orElse(null));

        // AMP 본문: article 내 모든 p를 모은다 (광고/내비는 p가 거의 없음)
        String ampContent = amp.select("article p, .article-body p, .story p")
                .stream().map(Element::text).filter(s -> !blank(s)).collect(Collectors.joining("\n\n"));

        LocalDate ampPublished = extractPublished(amp).orElse(published);

        if (!blank(ampTitle) && !blank(ampContent)) {
            NewsRequestDto dto = new NewsRequestDto();
            dto.setTitle(ampTitle.trim());
            dto.setContent(ampContent.trim());
            dto.setSource("조선비즈");
            dto.setPublishDate(ampPublished);
            return dto;
        }

        // 두 번 모두 실패하면 null
        return null;
    }

    /** 발행일 공통 추출: 메타 → time 태그 → empty */
    private Optional<LocalDate> extractPublished(Document d) {
        // og/arc 메타
        Optional<String> iso = meta(d, "meta[property=article:published_time]");
        if (iso.isEmpty()) iso = meta(d, "meta[name=article:published_time]");
        if (iso.isEmpty()) iso = meta(d, "meta[property=og:pubdate]");
        if (iso.isPresent()) {
            try { return Optional.of( java.time.OffsetDateTime.parse(iso.get()).toLocalDate() ); }
            catch (Exception ignore) {}
        }
        // <time datetime="...">
        Element t = d.selectFirst("time[datetime]");
        if (t != null) {
            String v = t.attr("datetime");
            try { return Optional.of( java.time.OffsetDateTime.parse(v).toLocalDate() ); }
            catch (Exception ignore) {}
        }
        return Optional.empty();
    }

    // RSS 전용 커넥션 (헤더 강화)
    private static Connection connectForRss(String url) {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Safari")
                .referrer("https://www.google.com")
                .header("Accept", "application/rss+xml, application/xml;q=0.9, */*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9")
                .timeout(10_000)
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
