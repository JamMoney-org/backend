package com.example.jammoney.ThemeLearning.controller;

import com.example.jammoney.ThemeLearning.dto.ThemeDto;
import com.example.jammoney.ThemeLearning.dto.TopicDetailDto;
import com.example.jammoney.ThemeLearning.dto.TopicListDto;
import com.example.jammoney.ThemeLearning.service.LearningTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class TopicController {
    private final LearningTopicService topicService;

    @GetMapping("/api/themes/{themeId}/topics")
    public List<TopicListDto> getTopics(@PathVariable Long themeId) {
        return topicService.getAllTopics(themeId);
    }

    @GetMapping("/api/themes/{themeId}/topics/{topicId}/details")
    public TopicDetailDto getDetails(@PathVariable long topicId) {
        return topicService.getTopicDetail(topicId);
    }
}
