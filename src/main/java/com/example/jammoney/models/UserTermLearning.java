package com.example.jammoney.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_term_learnings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTermLearning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "term_id")
    private FinancialTerm term;

    private boolean learned;
}
