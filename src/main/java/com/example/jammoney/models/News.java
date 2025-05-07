package com.example.jammoney.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String summary;
    private LocalDateTime publishedAt;
    private LocalDateTime savedAt;

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL)
    private List<NewsQuiz> quizzes;

    @ManyToMany
    @JoinTable(
            name = "news_key_terms",
            joinColumns = @JoinColumn(name = "news_id"),
            inverseJoinColumns = @JoinColumn(name = "term_id")
    )
    private Set<FinancialTerm> keyTerms = new HashSet<>();

}