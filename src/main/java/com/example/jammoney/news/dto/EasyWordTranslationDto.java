package com.example.jammoney.news.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EasyWordTranslationDto {
    private Long newsId;
    private String originalWord;
    private String translatedText;
    private String exampleSentence;
}
