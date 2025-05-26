package com.example.jammoney.news.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
//크롤링한 뉴스 데이터를 DB에 저장할 때
public class NewsRequestDto {
    private String title;
    private LocalDate publishDate;
    private String source;
    private String content;
}

