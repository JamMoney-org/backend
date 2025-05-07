package com.example.jammoney.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "financial_terms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String term;

    private String definition;

    private String category;

    @ElementCollection
    @OrderColumn(name = "sequence")
    private List<String> exampleSentences;

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    private List<FinancialTermQuiz> quizzes;
}
