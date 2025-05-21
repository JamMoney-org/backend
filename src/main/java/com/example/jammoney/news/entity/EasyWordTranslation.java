package com.example.jammoney.news.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "easy_word_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EasyWordTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalWord;

    @Column(length = 2000)
    private String translatedText;

    @ManyToOne
    @JoinColumn(name = "news_id")
    private News news;
}