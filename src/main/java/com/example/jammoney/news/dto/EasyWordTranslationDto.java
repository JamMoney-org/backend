package com.example.jammoney.news.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EasyWordTranslationDto {
    private Long newsId;
    private String originalWord;
    private String translatedText;
}
