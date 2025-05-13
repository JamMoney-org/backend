<<<<<<<< HEAD:src/main/java/com/example/jammoney/ThemeLearning/repository/ThemeRepository.java
package com.example.jammoney.ThemeLearning.repository;

import com.example.jammoney.ThemeLearning.models.Theme;
========
package com.example.jammoney.theme.repository;

import com.example.jammoney.theme.entity.Theme;
>>>>>>>> develop:src/main/java/com/example/jammoney/theme/repository/ThemeRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    Optional<Theme> findByName(String name);
    List<Theme> findAllByOrderByIdAsc();
}
