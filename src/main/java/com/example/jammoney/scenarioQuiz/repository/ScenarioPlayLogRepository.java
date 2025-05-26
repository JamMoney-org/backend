package com.example.jammoney.scenarioQuiz.repository;

import com.example.jammoney.scenarioQuiz.entity.Scenario;
import com.example.jammoney.scenarioQuiz.entity.ScenarioPlayLog;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioPlayLogRepository extends JpaRepository<ScenarioPlayLog, Long> {
    Optional<ScenarioPlayLog> findByScenarioAndUserAndStepOrder(Scenario scenario, User user, int stepOrder);

    // 전체 기록 히스토리 (정렬 포함)
    List<ScenarioPlayLog> findByScenarioAndUserOrderByStepOrderAsc(Scenario scenario, User user);
    void deleteByScenarioAndUser(Scenario scenario, User user);
}