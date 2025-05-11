package com.example.jammoney.ThemeLearning.repository;

import com.example.jammoney.ThemeLearning.models.LearningTopic;
import com.example.jammoney.ThemeLearning.models.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningTopicRepository extends JpaRepository<LearningTopic, Long> {
    List<LearningTopic> findByTheme(Theme theme);
}
