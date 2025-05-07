package com.example.jammoney.financeQuiz.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceQuizResponseDTO { //퀴즈 조회
    private Long id; //퀴즈 id
    private String question; //질문
    private List<String> options; //선지
    private String hint; //힌트
    private String difficulty; //난이도
    private String category; //카테고리
}
