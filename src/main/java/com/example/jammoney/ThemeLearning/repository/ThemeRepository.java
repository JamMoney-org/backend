package com.example.jammoney.ThemeLearning.repository;

import com.example.jammoney.ThemeLearning.models.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    Optional<Theme> findByName(String name);
}
