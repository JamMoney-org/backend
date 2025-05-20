package com.example.jammoney.financeTerm.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TermCreateDto { //단어 등룍용
    private String term;
    private String definition;
    private List<String> exampleSentences;
    private Long categoryId;
    private int dayIndex;
}
