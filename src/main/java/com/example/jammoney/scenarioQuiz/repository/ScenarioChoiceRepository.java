package com.example.jammoney.scenarioQuiz.repository;

import com.example.jammoney.scenarioQuiz.entity.ScenarioChoice;
import com.example.jammoney.scenarioQuiz.entity.ScenarioStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioChoiceRepository extends JpaRepository<ScenarioChoice, Long> {
    List<ScenarioChoice> findByStep(ScenarioStep step);
}