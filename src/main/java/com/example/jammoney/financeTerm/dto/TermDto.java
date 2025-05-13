package com.example.jammoney.financeTerm.dto;

import java.util.List;

public class TermDto {
    private Long termId;
    private String term;
    private String definition;
    private CategoryDto category;
    private int dayIndex;
    private List<String> exampleSentences;
    private boolean bookmarked;
    private boolean learned;
}
