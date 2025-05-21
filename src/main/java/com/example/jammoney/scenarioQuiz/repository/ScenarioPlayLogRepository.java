package com.example.jammoney.scenarioQuiz.repository;

import com.example.jammoney.scenarioQuiz.entity.Scenario;
import com.example.jammoney.scenarioQuiz.entity.ScenarioPlayLog;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioPlayLogRepository extends JpaRepository<ScenarioPlayLog, Long> {
    List<ScenarioPlayLog> findByUserAndScenarioOrderByStepOrderAsc(User user, Scenario scenario);
}