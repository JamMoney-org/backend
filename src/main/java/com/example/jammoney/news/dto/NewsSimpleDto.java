package com.example.jammoney.news.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
//뉴스 목록 리스트에서 보여줄 때 사용 (제목 + 날짜 + 출처만)
public class NewsSimpleDto {
    private Long id;
    private String title;
    private LocalDate publishDate;
    private String source;
}

