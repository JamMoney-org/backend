package com.example.jammoney.news.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "news_quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    private String option1;
    private String option2;
    private String option3;
    private String option4;

    private Integer correctAnswerIndex;

    @OneToOne
    @JoinColumn(name = "news_id")
    private News news;
}
