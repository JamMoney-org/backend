package com.example.jammoney.news.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NewsRequestDto {
    private String title;
    private LocalDate publishDate;
    private String source;
    private String content;
}

