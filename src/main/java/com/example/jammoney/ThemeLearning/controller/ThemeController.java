package com.example.jammoney.ThemeLearning.controller;

import com.example.jammoney.ThemeLearning.dto.ThemeDto;
import com.example.jammoney.ThemeLearning.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ThemeController {
    private final ThemeService themeService;

    @GetMapping("/api/themes")
    public List<ThemeDto> getThemes() {
        return themeService.getAllThemes();
    }
}
