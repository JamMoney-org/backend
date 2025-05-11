package com.example.jammoney.ThemeLearning.service;

import com.example.jammoney.ThemeLearning.dto.ThemeDto;
import com.example.jammoney.ThemeLearning.dto.TopicDetailDto;
import com.example.jammoney.ThemeLearning.dto.TopicListDto;
import com.example.jammoney.ThemeLearning.models.LearningTopic;
import com.example.jammoney.ThemeLearning.models.Theme;
import com.example.jammoney.ThemeLearning.repository.LearningTopicRepository;
import com.example.jammoney.ThemeLearning.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningTopicService {
    private final ThemeRepository themeRepository;
    private final LearningTopicRepository topicRepository;

    public List<TopicListDto> getAllTopics(Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("해당 카테고리가 없습니다"));

        return topicRepository.findByTheme(theme).stream()
                .map(topic -> {
                    TopicListDto dto = new TopicListDto();
                    dto.setTopicId(topic.getId());
                    dto.setTitle(topic.getTitle());
                    return dto;
                })
                .toList();
    }

    public TopicDetailDto getTopicDetail(Long topicId) {
        LearningTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("해당 토픽이 없습니다"));

        TopicDetailDto dto = new TopicDetailDto();
        dto.setTopicId(topic.getId());
        dto.setTitle(topic.getTitle());
        dto.setDescription(topic.getDescription());
        return dto;
    }
}
