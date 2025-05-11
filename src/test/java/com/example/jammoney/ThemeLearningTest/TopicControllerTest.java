package com.example.jammoney.ThemeLearningTest;

import com.example.jammoney.ThemeLearning.controller.ThemeController;
import com.example.jammoney.ThemeLearning.controller.TopicController;
import com.example.jammoney.ThemeLearning.dto.TopicDetailDto;
import com.example.jammoney.ThemeLearning.dto.TopicListDto;
import com.example.jammoney.ThemeLearning.service.LearningTopicService;
import com.example.jammoney.ThemeLearning.service.ThemeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TopicControllerTest {
    private MockMvc mockMvc;
    private LearningTopicService topicService;

    @BeforeEach
    void setup() {
        topicService = Mockito.mock(LearningTopicService.class);
        TopicController controller = new TopicController(topicService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getTopicsReturn() throws Exception {
        TopicListDto topic1 = new TopicListDto();
        topic1.setTopicId(1L);
        topic1.setTitle("첫 월급 관리법: 저축 vs 투자");

        TopicListDto topic2 = new TopicListDto();
        topic2.setTopicId(2L);
        topic2.setTitle("월급 재테크: 자동이체로 돈 모으기");

        List<TopicListDto> topicList = Arrays.asList(topic1, topic2);
        when(topicService.getAllTopics(1L)).thenReturn(topicList);

        mockMvc.perform(get("/api/themes/1/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].topicId").value(1))
                .andExpect(jsonPath("$[0].title").value("첫 월급 관리법: 저축 vs 투자"))
                .andExpect(jsonPath("$[1].topicId").value(2))
                .andExpect(jsonPath("$[1].title").value("월급 재테크: 자동이체로 돈 모으기"));
    }

    @Test
    void getTopicDetailReturn() throws Exception {
        TopicDetailDto detail = new TopicDetailDto();
        detail.setTopicId(1L);
        detail.setTitle("첫 월급 관리법: 저축 vs 투자");
        detail.setDescription("첫 월급은 단순한 수입이 아니라, 평생을 이어갈 재정 습관의 시작점입니다.");
        when(topicService.getTopicDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/themes/1/topics/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topicId").value(1))
                .andExpect(jsonPath("$.title").value("첫 월급 관리법: 저축 vs 투자"))
                .andExpect(jsonPath("$.description").value("첫 월급은 단순한 수입이 아니라, 평생을 이어갈 재정 습관의 시작점입니다."));
    }
}
