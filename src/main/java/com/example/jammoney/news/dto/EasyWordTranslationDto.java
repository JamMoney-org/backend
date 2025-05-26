package com.example.jammoney.news.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//쉬운말 번역
public class EasyWordTranslationDto {
    private Long newsId;
    private String originalWord;
    private String translatedText;
    private String exampleSentence;
}
