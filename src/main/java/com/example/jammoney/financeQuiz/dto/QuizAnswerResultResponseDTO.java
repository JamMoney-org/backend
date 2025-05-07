package com.example.jammoney.financeQuiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswerResultResponseDTO {
    private boolean isCorrect; //정답여부
    private String correctAnswer; //올바른답
    private String explanation; //해설
    private int expReward;     // 정답일 경우 경험치 보상
    private int coinReward;    // 정답일 경우 가상 머니 보상
}
