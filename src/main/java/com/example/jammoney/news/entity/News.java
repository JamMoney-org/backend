package com.example.jammoney.news.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "news",
        uniqueConstraints = @UniqueConstraint(
                columnNames = { "publish_date", "title" }
        )
)
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

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;

    @OneToOne(mappedBy = "news", cascade = CascadeType.ALL)
    private NewsQuiz quiz;
}

