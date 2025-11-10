package com.example.jammoney.scenarioQuiz.repository;

import com.example.jammoney.scenarioQuiz.entity.Scenario;
import com.example.jammoney.scenarioQuiz.entity.ScenarioStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioStepRepository extends JpaRepository<ScenarioStep, Long> {
    Optional<ScenarioStep> findByScenarioAndStepOrder(Scenario scenario, int stepOrder);
}