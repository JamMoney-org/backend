package com.example.jammoney.theme.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicListDto { //카테고리안 토픽
    private Long topicId;
    private String title;
    private String tag;
    private String imageUrl;
}

