package com.example.jammoney.news.repository;

import com.example.jammoney.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    void deleteByPublishDateBefore(LocalDate date);
    boolean existsByTitleAndPublishDate(String title, LocalDate publishDate);
    List<News> findByPublishDateAfter(LocalDate date);

}

