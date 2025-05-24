package com.example.jammoney.news.repository;

import com.example.jammoney.news.entity.News;
import com.example.jammoney.news.entity.NewsQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsQuizRepository extends JpaRepository<NewsQuiz, Long> {
    // 뉴스별로 퀴즈 찾아오기
    Optional<NewsQuiz> findByNews(News news);

    // 뉴스별로 퀴즈 삭제
    void deleteByNews(News news);
}

