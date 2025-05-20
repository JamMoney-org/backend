package com.example.jammoney.financeTerm.entity;

import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

@Entity
@Table(name = "user_quiz_day_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuizDayProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String categoryName;
    private int dayIndex;

    private boolean expGiven;
}
