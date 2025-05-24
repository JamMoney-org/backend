package com.example.jammoney.news.quiz.gpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GptNewsQuizResponseParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public GptNewsQuizData parse(String rawJson) {
        try {
            Map<String,Object> data = mapper.readValue(rawJson, Map.class);
            String question = (String) data.get("question");
            var options = (java.util.List<String>) data.get("options");
            int answerIndex = (int) data.get("answerIndex");
            return new GptNewsQuizData(question, options, answerIndex);
        } catch (Exception e) {
            throw new RuntimeException("퀴즈 파싱 실패: " + rawJson, e);
        }
    }
}
