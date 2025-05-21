package com.example.jammoney.news.controller;

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

    // 뉴스 목록 조회 (최근 7일치)
    @GetMapping
    public List<NewsSimpleDto> getRecentNewsList() {
        return newsService.getRecentNewsList();
    }

    // 뉴스 상세 조회
    @GetMapping("/{id}")
    public NewsResponseDto getNewsDetail(@PathVariable Long id) {
        return newsService.getNewsById(id);
    }
}
