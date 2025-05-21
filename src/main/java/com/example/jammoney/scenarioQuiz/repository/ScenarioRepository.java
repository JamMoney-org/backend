package com.example.jammoney.scenarioQuiz.repository;

import com.example.jammoney.scenarioQuiz.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
}