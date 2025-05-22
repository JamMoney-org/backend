package com.example.jammoney.scenarioQuiz.gpt.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GptNextMessageRequest {
    private String selectedContent;     // 방금 선택한 선택지 내용
    private List<String> history;      // 전체 선택 흐름
}