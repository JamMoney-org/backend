    package com.example.jammoney.scenarioQuiz.gpt;

    import org.springframework.stereotype.Component;

    import java.util.List;

    @Component
    public class GptPromptBuilder {

        // 1️⃣ 선택지 생성용 프롬프트
        public String buildChoicesPrompt(String topic, String aiMessage, List<String> history) {
            StringBuilder sb = new StringBuilder();

            sb.append("당신은 금융 교육을 위한 시나리오 기반 대화형 AI입니다.\n")
                    .append("사용자는 현실적인 금융 상황 속에서 선택지를 고르며 학습을 진행합니다.\n")
                    .append("각 선택지는 실제 대화처럼 자연스러우며, 선택 시 사용자에게 짧은 피드백이 함께 제공됩니다.\n")
                    .append("주제: ").append(topic).append("\n\n");

            if (history != null && !history.isEmpty()) {
                sb.append("[사용자의 선택 이력]\n");
                for (int i = 0; i < history.size(); i++) {
                    sb.append((i + 1)).append("단계: ").append(history.get(i)).append("\n");
                }
                sb.append("\n");
            }

            sb.append("[AI의 현재 질문]\n")
                    .append("\"").append(aiMessage).append("\"\n\n")

                    .append("위 질문에 대해 사용자가 고를 수 있는 현실적인 선택지 3~4개를 다음 JSON 형식으로 출력하세요.\n")
                    .append("각 선택지에는 아래 정보를 포함해야 합니다:\n")
                    .append("- content: 선택지 내용\n")
                    .append("- feedback: 선택했을 때 줄 짧은 피드백\n")
                    .append("- isGood: 좋은 선택이면 true, 애매하거나 위험한 선택이면 false\n")
                    .append("- isEnd: 이 선택 이후 시나리오가 종료된다면 true, 아니면 false\n\n")

                    .append("JSON 배열만 반환하세요. 예시:\n")
                    .append("[\n")
                    .append("  {\n")
                    .append("    \"content\": \"보증금 500만 원 이하 방 찾기\",\n")
                    .append("    \"feedback\": \"예산에 맞는 방을 고르는 건 좋은 출발입니다!\",\n")
                    .append("    \"isGood\": true,\n")
                    .append("    \"isEnd\": false\n")
                    .append("  }\n")
                    .append("]\n");

            return sb.toString();
        }

        // 2️⃣ 다음 질문 생성용 프롬프트
        public String buildNextMessagePrompt(String selectedChoice, List<String> history) {
            StringBuilder sb = new StringBuilder();
            sb.append("당신은 금융 시나리오 AI입니다.\n");
            sb.append("사용자의 이전 선택에 자연스럽게 이어지는 다음 질문을 생성하세요.\n");
            sb.append("질문은 친절하고 현실적인 어조로 작성하세요.\n");
            sb.append("단순한 문장 하나만 출력하고, JSON 없이 순수 문자열만 출력하세요.\n\n");

            sb.append("[사용자 선택 흐름]\n");
            for (int i = 0; i < history.size(); i++) {
                sb.append((i + 1)).append(". ").append(history.get(i)).append("\n");
            }

            sb.append("\n[방금 선택한 내용]\n");
            sb.append(selectedChoice).append("\n\n");

            sb.append("출력 예시: \"그렇다면 월세는 어느 정도까지 감당 가능하신가요?\"\n");

            return sb.toString();
        }

        // 3️⃣ 총평 생성용 프롬프트
        public String buildSummaryPrompt(List<String> history) {
            StringBuilder sb = new StringBuilder();
            sb.append("아래는 사용자의 금융 시나리오 선택 이력입니다:\n\n");
            for (int i = 0; i < history.size(); i++) {
                sb.append((i + 1)).append(". ").append(history.get(i)).append("\n");
            }

            sb.append("\n이 사용자의 전체 선택 흐름을 보고 금융적 판단이 얼마나 신중했는지 간단히 평가해 주세요.\n");
            sb.append("점수 없이 짧은 평가 문장 하나만 작성하세요.\n");

            return sb.toString();
        }
    }