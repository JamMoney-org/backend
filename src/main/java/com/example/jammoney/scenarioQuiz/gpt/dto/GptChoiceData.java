package com.example.jammoney.scenarioQuiz.gpt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GptChoiceData {
    private String content;     // 선택지 내용
    private String feedback;    // 선택에 대한 피드백

    @JsonProperty("isGood")
    private boolean isGood;     // 긍정적인 선택인지 여부
}