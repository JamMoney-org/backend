package com.example.jammoney.scenarioQuiz.repository;

import com.example.jammoney.scenarioQuiz.entity.Scenario;
import com.example.jammoney.scenarioQuiz.entity.ScenarioCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findByCategory(ScenarioCategory category);
}