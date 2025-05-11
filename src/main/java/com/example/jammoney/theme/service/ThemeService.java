package com.example.jammoney.theme.service;

import com.example.jammoney.theme.dto.ThemeDto;
import com.example.jammoney.theme.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;

    public List<ThemeDto> getAllThemes() {
        return themeRepository.findAllByOrderByIdAsc().stream()
                .map(theme -> {
                    ThemeDto dto = new ThemeDto();
                    dto.setThemeId(theme.getId());
                    dto.setName(theme.getName());
                    return dto;
                })
                .toList();
    }
}
