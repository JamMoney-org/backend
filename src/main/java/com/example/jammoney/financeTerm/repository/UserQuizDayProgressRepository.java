package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.financeTerm.entity.UserQuizDayProgress;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserQuizDayProgressRepository extends JpaRepository<UserQuizDayProgress, Long> {
    Optional<UserQuizDayProgress> findByUserAndCategoryNameAndDayIndex(User user, String categoryName, int dayIndex);
}
