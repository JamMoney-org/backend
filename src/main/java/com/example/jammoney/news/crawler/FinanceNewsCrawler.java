package com.example.jammoney.news.crawler;

import com.example.jammoney.news.dto.NewsRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FinanceNewsCrawler {

    private static final String UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/120 Safari/537.36";
    private static final int NEED_SUCCESS = 3;
    private static final int MAX_TRIES = 12;
    private static final int MIN_PARAS = 3;
    private static final int MAX_PARAS = 30;
    private static final int TIMEOUT_MS = 8000;
    private static final long BETWEEN_TRIES_MS = 500;

    public List<NewsRequestDto> fetchTodayNews() {
        log.info("[Jsoup 크롤링 시작]");
        List<NewsRequestDto> out = new ArrayList<>();
        try {
            // 1) 메인 페이지에서 링크 모으기
            Document main = get("https://biz.chosun.com/finance/");
            List<String> links = main.select("a.story-card__headline[href]")
                    .stream()
                    .map(el -> el.attr("abs:href"))
                    .filter(u -> u.startsWith("http"))
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));

            log.info("[메인] 링크 수(초기): {}", links.size());

            links = links.stream()
                    .filter(u -> u.contains("/stock/finance/") || u.contains("/finance/")
                            || u.matches(".*/\\d{4}/\\d{2}/\\d{2}/.*"))
                    .collect(Collectors.toCollection(ArrayList::new));

            log.info("[메인] 링크 수(필터 후): {}", links.size());

            int ok = 0, tries = 0;
            for (int i = 0; i < links.size() && tries < MAX_TRIES && ok < NEED_SUCCESS; i++) {
                String url = links.get(i);
                tries++;
                try {
                    NewsRequestDto dto = crawlOneViaAmp(url);
                    if (dto != null) {
                        out.add(dto);
                        ok++;
                        log.info("[{}] 수집 성공 (성공 {}/{}, 시도 {}/{})", i, ok, NEED_SUCCESS, tries, MAX_TRIES);
                    } else {
                        log.warn("[{}] 본문 없음 → 스킵: {}", i, url);
                    }
                } catch (Exception e) {
                    log.warn("[{}] 예외 스킵: {}: {}", i, e.getClass().getSimpleName(), e.getMessage());
                }
                try { Thread.sleep(BETWEEN_TRIES_MS); } catch (InterruptedException ignore) {}
            }
        } catch (Exception e) {
            log.error("[크롤링 전체 실패]", e);
        }
        log.info("[최종] 크롤링된 뉴스 개수: {}", out.size());
        return out;
    }

    private NewsRequestDto crawlOneViaAmp(String originalUrl) throws Exception {
        String amp = toAmpUrl(originalUrl);
        Document doc = get(amp);

        // 제목
        String title = textOrEmpty(doc.selectFirst("h1"));
        if (title.isBlank()) title = textOrEmpty(doc.selectFirst("header h1"));
        if (title.isBlank()) throw new IllegalStateException("title not found");

        // 본문 문단
        List<String> paras = doc.select("article p, main p, .article-body p, .article-content p")
                .stream()
                .map(el -> safeTrim(el.text()))
                .filter(s -> !s.isBlank())
                .limit(MAX_PARAS)
                .collect(Collectors.toList());

        if (paras.size() < MIN_PARAS) throw new IllegalStateException("AMP body too short");

        String content = String.join("\n\n", paras);
        NewsRequestDto dto = new NewsRequestDto();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setSource("조선비즈");
        dto.setPublishDate(LocalDate.now());
        return dto;
    }

    private static Document get(String url) throws Exception {
        Connection conn = Jsoup.connect(url)
                .userAgent(UA)
                .referrer("https://www.google.com/")
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .ignoreContentType(true) // 일부 CDN 헤더 이슈 회피
                .maxBodySize(2 * 1024 * 1024);

        // 간단 재시도 로직(최대 2회)
        int tries = 0;
        while (true) {
            tries++;
            try {
                Connection.Response res = conn.execute();
                int sc = res.statusCode();
                if (sc >= 200 && sc < 300) return res.parse();
                if (sc == 403 || sc == 429) {
                    // 봇차단: 잠깐 쉬고 한 번만 더
                    Thread.sleep(700);
                    if (tries >= 2) throw new IllegalStateException("blocked: " + sc);
                } else {
                    throw new IllegalStateException("http " + sc);
                }
            } catch (Exception ex) {
                if (tries >= 2) throw ex;
                Thread.sleep(400);
            }
        }
    }

    private static String toAmpUrl(String url) {
        if (url == null) return null;
        if (url.contains("biz.chosun.com") && !url.contains("/amp/")) {
            return url.replace("://biz.chosun.com/", "://biz.chosun.com/amp/");
        }
        return url;
    }

    private static String textOrEmpty(Element el) {
        return el == null ? "" : safeTrim(el.text());
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
