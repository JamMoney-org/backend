package com.example.jammoney.financeTerm.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizSubmitDto {
    private String categoryName;
    private int dayIndex;
    private List<QuizAnswerDto> answers;
}
