package com.example.jammoney.ThemeLearningTest;

import com.example.jammoney.theme.controller.ThemeController;
import com.example.jammoney.theme.dto.ThemeDto;
import com.example.jammoney.theme.service.ThemeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ThemeControllerTest {

    private MockMvc mockMvc;
    private ThemeService themeService;

    @BeforeEach
    void setup() {
        themeService = mock(ThemeService.class);
        ThemeController controller = new ThemeController(themeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getThemesReturn() throws Exception {
        ThemeDto theme1 = new ThemeDto();
        theme1.setThemeId(1L);
        theme1.setName("저축");

        ThemeDto theme2 = new ThemeDto();
        theme2.setThemeId(2L);
        theme2.setName("투자");

        List<ThemeDto> mockThemes = List.of(theme1, theme2);
        when(themeService.getAllThemes()).thenReturn(mockThemes);

        mockMvc.perform(get("/api/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("저축"))
                .andExpect(jsonPath("$[1].name").value("투자"));
    }
}
