package com.example.jammoney.news.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
//사용자가 뉴스를 클릭해서 상세 내용을 볼 때 사용
public class NewsRequestDto {
    private String title;
    private LocalDate publishDate;
    private String source;
    private String content;
}

