package com.example.jammoney.news.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
//뉴스에 포함된 퀴즈 내용 전용 DTO
public class NewsQuizDto {
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private Integer correctAnswerIndex;
}
