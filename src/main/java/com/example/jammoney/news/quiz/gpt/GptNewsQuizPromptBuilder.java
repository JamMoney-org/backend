package com.example.jammoney.news.quiz.gpt;

import org.springframework.stereotype.Component;

@Component
public class GptNewsQuizPromptBuilder {
    public String buildQuizPrompt(String newsContent) {
        return new StringBuilder()
                .append("다음 뉴스 내용을 읽고 금융 관련 퀴즈 한 문제를 내주세요.\n\n")
                .append(newsContent).append("\n\n")
                .append("– 문제: 객관식 한 문제\n")
                .append("– 선택지 4개 (보기 번호 1~4)\n")
                .append("– 정답 인덱스 (1부터 시작)\n")
                .append("JSON 형식으로, 아래 예시처럼만 반환하세요:\n")
                .append("{\n")
                .append("  \"question\": \"…\",\n")
                .append("  \"options\": [\"…\",\"…\",\"…\",\"…\"],\n")
                .append("  \"answerIndex\": 2\n")
                .append("}\n")
                .toString();
    }
}
