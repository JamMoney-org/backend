package com.example.jammoney.financeTerm.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class TermDto { //DAY별 단어 리스트를 조회
    private Long termId;
    private String term;
    private String definition;
    private CategoryDto category;
    private int dayIndex;
    private List<String> exampleSentences;
    private boolean bookmarked;
    private boolean learned;
}
