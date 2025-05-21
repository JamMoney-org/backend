package com.example.jammoney.scenarioQuiz.entity;

import com.example.jammoney.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioPlayLog { //사용자의 선택 이력 (총평용)
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Scenario scenario;

    @ManyToOne
    private ScenarioStep step;

    @ManyToOne
    private ScenarioChoice choice;

    private int stepOrder;

    private LocalDateTime selectedAt;
}