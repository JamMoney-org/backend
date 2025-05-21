package com.example.jammoney.financeTerm.entity;

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
    private int correctAnswer;

    @ElementCollection
    @OrderColumn(name = "sequence")
    private List<String> choices;

    @ManyToOne
    @JoinColumn(name = "term_id")
    private FinancialTerm term;
}