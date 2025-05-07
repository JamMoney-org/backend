package com.example.jammoney.financeQuiz.dto;

import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceQuizCreateRequestDTO { //퀴즈 생성
    private String question; //질문
    private List<String> options; //선지
    private int correctIndex; //답 인덱스
    private String hint; //힌트
    private String explanation; //해설
    private String difficulty; //난이도
    private String category; //카테고리
}