package com.example.jammoney.theme.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicDetailDto { //토픽 상세 내용
    private Long topicId;
    private String title;
    private String description;
    private String tag;
}

