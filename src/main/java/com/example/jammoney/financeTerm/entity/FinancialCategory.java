package com.example.jammoney.financeTerm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "financial_categorys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String category;

}
