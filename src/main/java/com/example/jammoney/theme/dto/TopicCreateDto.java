package com.example.jammoney.theme.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicCreateDto {
    private Long themeId;
    private String title;
    private String description;
}
