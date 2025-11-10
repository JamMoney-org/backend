package com.example.jammoney.news.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NewsResponseDto {
    private Long id;
    private String title;
    private LocalDate publishDate;
    private String source;
    private String content;
    private String summary;
    private NewsQuizDto quiz;
}
