package com.example.jammoney.theme.controller;

import com.example.jammoney.theme.dto.TopicCreateDto;
import com.example.jammoney.theme.dto.TopicDetailDto;
import com.example.jammoney.theme.dto.TopicListDto;
import com.example.jammoney.theme.entity.LearningTopic;
import com.example.jammoney.theme.service.LearningTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class TopicController { //카테고리 선택 후 토픽 리스트 띄움
    private final LearningTopicService topicService;

    //토픽 리스트
    @GetMapping("/api/themes/{themeId}/topics")
    public List<TopicListDto> getTopics(@PathVariable Long themeId) {
        return topicService.getAllTopics(themeId);
    }

    //토픽 선택시 자세한 내용
    @GetMapping("/api/themes/{themeId}/topics/{topicId}/details")
    public TopicDetailDto getDetails(@PathVariable long topicId) {
        return topicService.getTopicDetail(topicId);
    }

    //내용 생성
    @PostMapping("/api/themes/topics")
    public void createTopic(@RequestBody TopicCreateDto createDto) {
        topicService.createTopic(createDto);
    }
}
