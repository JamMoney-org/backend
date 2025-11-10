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
    private static final List<String> RSS_URLS = List.of(
            "https://www.chosun.com/arc/outboundfeeds/rss/category/economy/?outputType=xml",
            "https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml"
    );

    private static final int TIMEOUT_MS = 10_000;
    private static final int MAX_ITEMS  = 3;
    private static final int NEWS_POOL = 10;

    public List<NewsRequestDto> fetchTodayNews() {
        log.info("[크롤링 시작]");
        List<NewsRequestDto> out = new ArrayList<>();

        try {
            List<String> links = fetchLinksFromRss(MAX_ITEMS * NEWS_POOL);
            log.info("[RSS] 후보 링크 수: {}", links.size());

            for (String link : links) {
                if (out.size() >= MAX_ITEMS) break;
                try {
                    NewsRequestDto dto = parseArticle(link);
                    if (dto == null) {
                        log.warn("[스킵] 요소 파싱 실패 {}", link);
                        continue;
                    }
                    out.add(dto);
                    log.info("[수집] {} (총 {}개)", dto.getTitle(), out.size());
                } catch (Exception e) {
                    log.warn("[스킵] 단건 실패 {} - {}", link, e.toString());
                }
            }
        } catch (Exception e) {
            log.error("[크롤링 전체 실패]", e);
        }

        log.info("[최종] 크롤링된 뉴스 개수: {}", out.size());
        return out;
    }
    private List<String> fetchLinksFromRss(int want) {
        List<String> acc = new ArrayList<>();
        for (String rss : RSS_URLS) {
            try {
                var res = connectForRss(rss).execute();
                if (res.statusCode() / 100 != 2) {
                    log.warn("[RSS HTTP {}] {}", res.statusCode(), rss);
                    continue;
                }
                Document doc = res.parse();
                doc.select("item > link").stream()
                        .map(Element::text)
                        .filter(u -> u != null && u.startsWith("http"))
                        .forEach(acc::add);
                doc.select("entry > link[href]").stream()
                        .map(e -> e.attr("href"))
                        .filter(u -> u != null && u.startsWith("http"))
                        .forEach(acc::add);

            } catch (Exception e) {
                log.warn("[RSS 실패] {} - {}", rss, e.toString());
            }
            if (acc.size() >= want * 2) break;
        }
        return acc.stream()
                .filter(u -> u.contains("/economy/stock-finance/"))
                .filter(u -> !u.matches(".*/(photo|video|multimedia|vod|gallery|image)/.*"))
                .distinct()
                .limit(want)
                .collect(Collectors.toList());
    }

    private NewsRequestDto parseArticle(String url) throws Exception {
        // 기본 뷰
        Document doc = connectHtml(url).get();
        String title = optText(doc, "h1.article-header__headline")
                .orElseGet(() -> optText(doc, "h1.article-title").orElse(null));
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

        String ampUrl = url.contains("outputType=amp") ? url
                : url + (url.contains("?") ? "&" : "?") + "outputType=amp";
        Document amp = connectHtml(ampUrl).get();

        String ampTitle = optText(amp, "h1")
                .orElseGet(() -> optText(amp, "header h1").orElse(title));
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
        return null;
    }

    private Optional<LocalDate> extractPublished(Document d) {
        Optional<String> iso = meta(d, "meta[property=article:published_time]");
        if (iso.isEmpty()) iso = meta(d, "meta[name=article:published_time]");
        if (iso.isEmpty()) iso = meta(d, "meta[property=og:pubdate]");
        if (iso.isPresent()) {
            try { return Optional.of(OffsetDateTime.parse(iso.get()).toLocalDate()); }
            catch (Exception ignore) {}
        }
        Element t = d.selectFirst("time[datetime]");
        if (t != null) {
            String v = t.attr("datetime");
            try { return Optional.of(OffsetDateTime.parse(v).toLocalDate()); }
            catch (Exception ignore) {}
        }
        return Optional.empty();
    }

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
