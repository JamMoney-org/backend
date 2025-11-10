package com.example.jammoney.news.repository;

import com.example.jammoney.news.entity.News;
import com.example.jammoney.news.entity.NewsQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsQuizRepository extends JpaRepository<NewsQuiz, Long> {
    Optional<NewsQuiz> findByNews(News news);
    void deleteByNews(News news);
}

