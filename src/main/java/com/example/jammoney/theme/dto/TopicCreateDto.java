package com.example.jammoney.theme.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicCreateDto { //토픽 생성용 DTO
    private Long themeId;
    private String title;
    private String description;
}
