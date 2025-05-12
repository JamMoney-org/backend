package com.example.jammoney.ThemeLearningTest;

import com.example.jammoney.theme.controller.TopicController;
import com.example.jammoney.theme.dto.TopicCreateDto;
import com.example.jammoney.theme.dto.TopicDetailDto;
import com.example.jammoney.theme.dto.TopicListDto;
import com.example.jammoney.theme.service.LearningTopicService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TopicControllerTest {
    private MockMvc mockMvc;
    private LearningTopicService topicService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        topicService = Mockito.mock(LearningTopicService.class);
        TopicController controller = new TopicController(topicService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
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

    @Test
    void createTopicReturnOk() throws Exception {
        TopicCreateDto dto = new TopicCreateDto();
        dto.setThemeId(1L);
        dto.setTitle("청소년을 위한 금융기초");
        dto.setDescription("청소년 눈높이에 맞춘 소비와 저축의 기초");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/themes/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

}
