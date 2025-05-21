package com.example.jammoney.news.service;

import com.example.jammoney.news.dto.NewsQuizDto;
import com.example.jammoney.news.dto.NewsRequestDto;
import com.example.jammoney.news.dto.NewsResponseDto;
import com.example.jammoney.news.dto.NewsSimpleDto;
import com.example.jammoney.news.entity.News;
import com.example.jammoney.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;
    public void deleteOldNews() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        newsRepository.deleteByPublishDateBefore(sevenDaysAgo);
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

    public NewsResponseDto getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("뉴스를 찾을 수 없습니다. id=" + id));

        NewsResponseDto dto = new NewsResponseDto();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setPublishDate(news.getPublishDate());
        dto.setSource(news.getSource());
        dto.setContent(news.getContent());
        dto.setSummary(news.getSummary());
        // 퀴즈 포함할 경우 추가
        if (news.getQuiz() != null) {
            dto.setQuiz(NewsQuizDto.builder()
                    .question(news.getQuiz().getQuestion())
                    .option1(news.getQuiz().getOption1())
                    .option2(news.getQuiz().getOption2())
                    .option3(news.getQuiz().getOption3())
                    .option4(news.getQuiz().getOption4())
                    .correctAnswerIndex(news.getQuiz().getCorrectAnswerIndex())
                    .build());
        }
        return dto;
    }

}
