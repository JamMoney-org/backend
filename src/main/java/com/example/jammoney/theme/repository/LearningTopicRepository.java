package com.example.jammoney.theme.repository;

import com.example.jammoney.theme.entity.LearningTopic;
import com.example.jammoney.theme.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningTopicRepository extends JpaRepository<LearningTopic, Long> {
    List<LearningTopic> findByTheme(Theme theme);
}
