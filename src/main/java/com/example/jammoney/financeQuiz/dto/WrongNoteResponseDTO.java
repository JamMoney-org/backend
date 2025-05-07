package com.example.jammoney.financeQuiz.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrongNoteResponseDTO { //오답노트
    private Long quizId;
    private String question;
    private List<String> options;
    private int correctIndex;
    private int userAnswerIndex;
    private String explanation;
}
