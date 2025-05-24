package com.example.jammoney.news.repository;

import com.example.jammoney.news.entity.EasyWordTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EasyWordTranslationRepository extends JpaRepository<EasyWordTranslation, Long> {
    List<EasyWordTranslation> findByNewsId(Long newsId);
}
