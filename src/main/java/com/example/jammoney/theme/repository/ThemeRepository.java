package com.example.jammoney.theme.repository;

import com.example.jammoney.theme.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    Optional<Theme> findByName(String name);
    List<Theme> findAllByOrderByIdAsc();
}
