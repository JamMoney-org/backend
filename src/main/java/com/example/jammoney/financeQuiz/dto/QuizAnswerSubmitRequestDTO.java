package com.example.jammoney.financeQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswerSubmitRequestDTO { //사용자 정답 제출 DTO
    private Long quizId; //퀴즈 id
    private int selectedIndex; //고른 답
}
