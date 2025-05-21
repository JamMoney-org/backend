package com.example.jammoney.news.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    private LocalDate publishDate;

    private String source;

    @Column(length = 10000)
    private String content;

    @Column(length = 3000)
    private String summary;

    @OneToOne(mappedBy = "news", cascade = CascadeType.ALL)
    private NewsQuiz quiz;
}

