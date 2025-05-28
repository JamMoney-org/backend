package com.example.jammoney.news.service;

import com.example.jammoney.news.crawler.FinanceNewsCrawler;
import com.example.jammoney.news.dto.NewsQuizDto;
import com.example.jammoney.news.dto.NewsRequestDto;
import com.example.jammoney.news.dto.NewsResponseDto;
import com.example.jammoney.news.dto.NewsSimpleDto;
import com.example.jammoney.news.entity.News;
import com.example.jammoney.news.quiz.service.NewsQuizService;
import com.example.jammoney.news.repository.NewsRepository;
import com.example.jammoney.news.summary.SummaryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;
    private final FinanceNewsCrawler financeNewsCrawler;
    private final SummaryClient gptSummaryClient;
    private final NewsQuizService newsQuizService;

    public void deleteOldNews() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        newsRepository.deleteByPublishDateBefore(sevenDaysAgo);
    }

    public long countByPublishDate(LocalDate date) {
        return newsRepository.countByPublishDate(date);
    }

    public List<NewsRequestDto> testCrawlingPreview() {
        return financeNewsCrawler.fetchTodayNews();
    }

    public void saveNewsList(List<NewsRequestDto> dtoList) {
        List<News> newsList = dtoList.stream()
                .filter(dto -> !newsRepository.existsByTitleAndPublishDate(dto.getTitle(), dto.getPublishDate()))
                .map(dto -> News.builder()
                        .title(dto.getTitle())
                        .publishDate(dto.getPublishDate())
                        .source(dto.getSource())
                        .content(dto.getContent())
                        .build())
                .collect(Collectors.toList());

        newsRepository.saveAll(newsList);
    }

    public List<NewsSimpleDto> getRecentNewsList() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        return newsRepository.findByPublishDateAfter(sevenDaysAgo)
                .stream()
                .map(news -> {
                    NewsSimpleDto dto = new NewsSimpleDto();
                    dto.setId(news.getId());
                    dto.setTitle(news.getTitle());
                    dto.setPublishDate(news.getPublishDate());
                    dto.setSource(news.getSource());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public NewsResponseDto getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("뉴스를 찾을 수 없습니다. id=" + id));

        if (news.getSummary() == null) {
            String summary = gptSummaryClient.summarize(news.getContent());
            news.setSummary(summary);
            newsRepository.save(news);
        }

        NewsQuizDto quizDto = newsQuizService.generateAndSaveQuiz(id);

        NewsResponseDto dto = new NewsResponseDto();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setPublishDate(news.getPublishDate());
        dto.setSource(news.getSource());
        dto.setContent(news.getContent());
        dto.setSummary(news.getSummary());
        dto.setQuiz(quizDto);

        return dto;
    }

    @Transactional
    public String generateAndSaveSummary(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("뉴스를 찾을 수 없습니다. id=" + newsId));

        if (news.getSummary() != null) {
            return news.getSummary();
        }

        String summary = gptSummaryClient.summarize(news.getContent());

        news.setSummary(summary);
        newsRepository.save(news);

        return summary;
    }
}
