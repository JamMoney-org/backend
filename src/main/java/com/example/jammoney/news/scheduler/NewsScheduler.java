package com.example.jammoney.news.scheduler;

import com.example.jammoney.news.crawler.FinanceNewsCrawler;
import com.example.jammoney.news.dto.NewsRequestDto;
import com.example.jammoney.news.service.NewsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final FinanceNewsCrawler crawler;
    private final NewsService newsService;

    @Scheduled(cron = "0 0 8 * * *") // 매일 오전 6시 실행
    public void crawlAndSave() {
        newsService.deleteOldNews(); // 7일 초과 뉴스 삭제
        List<NewsRequestDto> todayNews = crawler.fetchTodayNews();
        newsService.saveNewsList(todayNews);
    }

    @PostConstruct
    public void testCrawlingManually() {
        System.out.println("[INFO] 수동 크롤링 테스트 시작");
        newsService.deleteOldNews();
        List<NewsRequestDto> todayNews = crawler.fetchTodayNews();
        newsService.saveNewsList(todayNews);
        System.out.println("[INFO] 수동 크롤링 및 저장 완료");
    }
}
