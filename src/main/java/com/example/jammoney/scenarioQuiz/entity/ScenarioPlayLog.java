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
public class ScenarioPlayLog {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Scenario scenario;

    @ManyToOne
    private ScenarioStep step;

    private String choiceContent;

    private int stepOrder;
    private LocalDateTime selectedAt;
}