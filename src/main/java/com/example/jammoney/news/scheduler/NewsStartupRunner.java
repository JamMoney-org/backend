package com.example.jammoney.news.scheduler;

import com.example.jammoney.news.crawler.FinanceNewsCrawler;
import com.example.jammoney.news.dto.NewsRequestDto;
import com.example.jammoney.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsStartupRunner implements ApplicationListener<ApplicationReadyEvent> {

    private final FinanceNewsCrawler crawler;
    private final NewsService newsService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LocalDate today = LocalDate.now();
        long countToday = newsService.countByPublishDate(today);

        // 오전 8시 이후에, 오늘 크롤된 뉴스가 3개 미만이면 보충
        List<NewsRequestDto> missing = crawler.fetchTodayNews();
        newsService.saveNewsList(missing);
//        if (LocalTime.now().isAfter(LocalTime.of(8, 0)) && countToday < 3) {
//            List<NewsRequestDto> missing = crawler.fetchTodayNews();
//            newsService.saveNewsList(missing);
//        }
    }
}
