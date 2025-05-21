package com.example.jammoney.news.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
//뉴스에 포함된 퀴즈 내용 전용 DTO
public class NewsSimpleDto {
    private Long id;
    private String title;
    private LocalDate publishDate;
    private String source;
}

