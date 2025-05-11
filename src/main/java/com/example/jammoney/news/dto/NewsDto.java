package com.example.jammoney.news.dto;

import java.time.LocalDateTime;
import java.util.List;

public class NewsDto {

    private Long newsId;

    private String title;
    private String content;
    private String summary;
    private LocalDateTime publishedAt;

    private List<NewsTermDto> keyTermList;

    private List<NewsQuizDto> quizzes;
}
