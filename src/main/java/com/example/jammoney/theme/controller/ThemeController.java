package com.example.jammoney.theme.controller;

import com.example.jammoney.theme.dto.ThemeDto;
import com.example.jammoney.theme.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ThemeController { //카테고리 선택
    private final ThemeService themeService;

    @GetMapping("/api/themes")
    public List<ThemeDto> getThemes() {
        return themeService.getAllThemes();
    }
}
