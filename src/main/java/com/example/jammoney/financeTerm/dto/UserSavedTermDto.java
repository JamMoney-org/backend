package com.example.jammoney.financeTerm.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class UserSavedTermDto { //나만의 단어장
    private Long termId;
    private String term;
    private String definition;
    private List<String> exampleSentences;
}
