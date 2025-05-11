package com.example.jammoney.ThemeLearningTest;

import com.example.jammoney.theme.dto.TopicDetailDto;
import com.example.jammoney.theme.dto.TopicListDto;
import com.example.jammoney.theme.entity.LearningTopic;
import com.example.jammoney.theme.entity.Theme;
import com.example.jammoney.theme.repository.LearningTopicRepository;
import com.example.jammoney.theme.repository.ThemeRepository;
import com.example.jammoney.theme.service.LearningTopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LearningTopicServiceTest {
    private ThemeRepository themeRepository;
    private LearningTopicRepository topicRepository;
    private LearningTopicService topicService;

    @BeforeEach
    void setup() {
        themeRepository = mock(ThemeRepository.class);
        topicRepository = mock(LearningTopicRepository.class);
        topicService = new LearningTopicService(themeRepository, topicRepository);
    }

    @Test
    void getAllTopicsReturn() {
        Theme theme = Theme.builder().id(1L).name("저축").build();

        LearningTopic topic1 = LearningTopic.builder()
                .id(1L).title("첫 월급 관리법: 저축 vs 투자").theme(theme).build();
        LearningTopic topic2 = LearningTopic.builder()
                .id(2L).title("월급 재테크: 자동이체로 돈 모으기").theme(theme).build();
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(topicRepository.findByTheme(theme)).thenReturn(Arrays.asList(topic1, topic2));

        List<TopicListDto> result = topicService.getAllTopics(1L);
        assertEquals(2, result.size());
        assertEquals("첫 월급 관리법: 저축 vs 투자", result.get(0).getTitle());
        assertEquals(1L, result.get(0).getTopicId());
        assertEquals("월급 재테크: 자동이체로 돈 모으기", result.get(1).getTitle());
        assertEquals(2L, result.get(1).getTopicId());
    }

    @Test
    void getTopicDetailReturn() {
        LearningTopic topic = LearningTopic.builder()
                .id(1L)
                .title("첫 월급 관리법: 저축 vs 투자")
                .description("첫 월급은 단순한 수입이 아니라, 평생을 이어갈 재정 습관의 시작점입니다. ")
                .build();

        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
        TopicDetailDto dto = topicService.getTopicDetail(1L);
        assertEquals("첫 월급 관리법: 저축 vs 투자", dto.getTitle());
        assertEquals("첫 월급은 단순한 수입이 아니라, 평생을 이어갈 재정 습관의 시작점입니다. ", dto.getDescription());
        assertEquals(1L, dto.getTopicId());
    }

    @Test
    void getAllTopics_ThrowsException() {
        when(themeRepository.findById(100L)).thenReturn(Optional.empty());
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            topicService.getAllTopics(100L);
        });
        assertEquals("해당 카테고리가 없습니다", e.getMessage());
    }

    @Test
    void getTopicDetailThrowsException() {
        when(topicRepository.findById(100L)).thenReturn(Optional.empty());
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            topicService.getTopicDetail(100L);
        });
        assertEquals("해당 토픽이 없습니다", e.getMessage());
    }
}
