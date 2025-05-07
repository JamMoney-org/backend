package com.example.jammoney.models;

import com.example.jammoney.User.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_saved_terms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSavedTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "term_id")
    private FinancialTerm term;
}

