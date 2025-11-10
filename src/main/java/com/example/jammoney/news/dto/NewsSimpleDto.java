package com.example.jammoney.news.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NewsSimpleDto {
    private Long id;
    private String title;
    private LocalDate publishDate;
    private String source;
}

