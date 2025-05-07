package com.example.jammoney.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learning_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;
}
