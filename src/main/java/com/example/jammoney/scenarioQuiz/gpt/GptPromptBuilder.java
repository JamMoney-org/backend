package com.example.jammoney.scenarioQuiz.gpt;

import com.example.jammoney.financeQuiz.entity.Difficulty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GptPromptBuilder {

    // 1️⃣ 선택지 생성용 프롬프트
    public String buildChoicesPrompt(String topic, String aiMessage, List<String> history, Difficulty difficulty) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 금융 교육을 위한 시나리오 기반 대화형 AI입니다.\n")
                .append("이 시나리오는 총 4단계로 구성됩니다.\n")
                .append("각 단계에서 사용자는 현실적인 금융 상황 속 선택지를 고르며 학습을 진행합니다.\n")
                .append("선택지는 실제 대화처럼 자연스럽고, 선택 시 간단한 피드백이 포함됩니다.\n")
                .append("난이도: ").append(difficulty.name()).append("\n")
                .append("주제: ").append(topic).append("\n\n");

        if (history != null && !history.isEmpty()) {
            sb.append("[사용자 선택 이력]\n");
            for (int i = 0; i < history.size(); i++) {
                sb.append((i + 1)).append("단계: ").append(history.get(i)).append("\n");
            }
            sb.append("\n");

            // ✅ 마지막 질문 시 종료 선택지 포함 안내
            if (history.size() == 3) {
                sb.append("※ 현재는 마지막 4단계입니다.\n")
                        .append("이번 질문 이후 시나리오가 종료되므로, 적어도 하나의 선택지에 \"isEnd\": true 를 포함하세요.\n")
                        .append("예: \"이 카드를 선택하겠습니다.\" 같은 마무리 선택지\n\n");
            }
        }

        sb.append("[AI의 현재 질문]\n")
                .append("\"").append(aiMessage).append("\"\n\n")
                .append("위 질문에 대해 사용자가 고를 수 있는 선택지 3~4개를 다음 JSON 형식으로 출력하세요.\n")
                .append("각 선택지에는 다음 정보를 포함하세요:\n")
                .append("- content: 선택지 내용\n")
                .append("- feedback: 선택했을 때 줄 짧은 피드백\n")
                .append("- isGood: 좋은 선택이면 true, 애매하거나 위험한 선택이면 false\n")
                .append("- isEnd: 이 선택 이후 시나리오가 종료되면 true, 아니면 false\n")
                .append("JSON 배열만 반환하세요. 예시:\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"content\": \"연회비가 없는 카드를 선택한다\",\n")
                .append("    \"feedback\": \"초기 부담이 적은 선택이에요!\",\n")
                .append("    \"isGood\": true,\n")
                .append("    \"isEnd\": false\n")
                .append("  }\n")
                .append("]");

        return sb.toString();
    }

    // 2️⃣ 다음 질문 생성용 프롬프트
    public String buildNextMessagePrompt(String conversationHistory, String selectedChoice, Difficulty difficulty) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 ").append(difficulty.name()).append(" 난이도의 금융 시나리오 AI입니다.\n")
                .append("이 시나리오는 총 4단계로 구성되며, 단계별로 자연스럽고 친절한 질문을 생성해야 합니다.\n")
                .append("현재 대화 흐름과 사용자의 마지막 선택을 바탕으로, 다음 단계의 질문을 하나 출력하세요.\n")
                .append("대화가 마지막 단계(4단계)라면, 마무리 질문처럼 보여야 합니다.\n")
                .append("JSON 없이 순수한 문장 하나만 출력하세요.\n\n");

        sb.append("[전체 대화 흐름]\n")
                .append(conversationHistory).append("\n\n");

        sb.append("[사용자의 방금 선택]\n")
                .append(selectedChoice).append("\n");

        return sb.toString();
    }

    // 3️⃣ 총평 생성용 프롬프트
    public String buildSummaryPrompt(List<String> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("아래는 사용자의 금융 시나리오 선택 이력입니다:\n\n");
        for (int i = 0; i < history.size(); i++) {
            sb.append((i + 1)).append(". ").append(history.get(i)).append("\n");
        }

        sb.append("\n이 사용자의 선택 흐름을 기반으로 금융적 판단이 얼마나 신중했는지를 간단히 평가하세요.\n")
                .append("점수 없이, 격려 또는 조언이 포함된 한 문장의 평가만 작성하세요.\n");

        return sb.toString();
    }
}