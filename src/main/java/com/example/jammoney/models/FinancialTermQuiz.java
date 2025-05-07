package com.example.jammoney.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "financial_term_quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTermQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;
    private String correctAnswer;

    @ElementCollection
    private List<String> choices;

    @ManyToOne
    @JoinColumn(name = "term_id")
    private FinancialTerm term;
}