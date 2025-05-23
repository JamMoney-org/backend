package com.example.jammoney.theme.service;

import com.example.jammoney.theme.dto.TopicCreateDto;
import com.example.jammoney.theme.dto.TopicDetailDto;
import com.example.jammoney.theme.dto.TopicListDto;
import com.example.jammoney.theme.entity.LearningTopic;
import com.example.jammoney.theme.entity.Theme;
import com.example.jammoney.theme.repository.LearningTopicRepository;
import com.example.jammoney.theme.repository.ThemeRepository;
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
                    dto.setTag(topic.getTag()); // tag 포함
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
        dto.setTag(topic.getTag()); // tag 포함
        return dto;
    }

    public void createTopic(TopicCreateDto dto) {
        Theme theme = themeRepository.findById(dto.getThemeId())
                .orElseThrow(() -> new RuntimeException("해당 테마가 없습니다"));

        LearningTopic topic = LearningTopic.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .tag(dto.getTag()) // tag 포함
                .theme(theme)
                .build();

        topicRepository.save(topic);
    }
}
