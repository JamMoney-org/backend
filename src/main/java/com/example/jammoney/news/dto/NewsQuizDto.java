package com.example.jammoney.news.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewsQuizDto { //크롤링한 뉴스 데이터를 DB에 저장할 때
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private Integer correctAnswerIndex;
}
