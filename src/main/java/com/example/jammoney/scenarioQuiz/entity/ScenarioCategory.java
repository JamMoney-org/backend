package com.example.jammoney.scenarioQuiz.entity;

public enum ScenarioCategory {
    CONSUMPTION("소비"),
    SAVING("저축"),
    LOAN("대출"),
    INVESTMENT("투자"),
    TAX("세금");

    private final String label;

    ScenarioCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}