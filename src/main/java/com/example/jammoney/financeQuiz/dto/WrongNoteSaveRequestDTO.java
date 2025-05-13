package com.example.jammoney.financeQuiz.dto;

import com.example.jammoney.financeQuiz.entity.QuizCategory;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrongNoteSaveRequestDTO {
    private Long userId;
    private String question;
    private List<String> options;
    private int correctIndex;
    private int chosenIndex;
    private String explanation;
    private QuizCategory category;
}