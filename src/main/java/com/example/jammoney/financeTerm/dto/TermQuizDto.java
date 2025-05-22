package com.example.jammoney.financeTerm.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class TermQuizDto { //퀴즈 목록 불러올 때 사용
    private Long quizId;
    private String question;
    private List<String> choices;
}

