package com.example.jammoney.financeTerm.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class TermQuizDto {
    private Long quizId;
    private String question;
    private List<String> choices;
}

