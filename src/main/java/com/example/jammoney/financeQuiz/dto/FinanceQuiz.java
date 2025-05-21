package com.example.jammoney.financeQuiz.dto;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceQuiz {
    private String question; //질문
    private List<String> options; //선지
    private int correctIndex; //답 인덱스
    private String hint; //힌트
    private String explanation; //해설
    private Difficulty difficulty; //난이도
    private QuizCategory category; //카테고리
}