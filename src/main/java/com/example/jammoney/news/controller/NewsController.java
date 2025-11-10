package com.example.jammoney.news.controller;

import com.example.jammoney.news.dto.NewsRequestDto;
import com.example.jammoney.news.dto.NewsResponseDto;
import com.example.jammoney.news.dto.NewsSimpleDto;
import com.example.jammoney.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public List<NewsSimpleDto> getRecentNewsList() {
        return newsService.getRecentNewsList();
    }

    @GetMapping("/{id}")
    public NewsResponseDto getNewsDetail(@PathVariable Long id) {
        return newsService.getNewsById(id);
    }

    @GetMapping("/crawl/test")
    public List<NewsRequestDto> testCrawlOnly() {
        return newsService.testCrawlingPreview();
    }

    @PostMapping("/{id}/summary")
    public String generateSummary(@PathVariable("id") Long newsId) {
        return newsService.generateAndSaveSummary(newsId);
    }
}
