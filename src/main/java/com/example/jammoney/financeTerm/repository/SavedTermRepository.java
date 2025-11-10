package com.example.jammoney.financeTerm.repository;

import com.example.jammoney.financeTerm.entity.FinancialTerm;
import com.example.jammoney.financeTerm.entity.UserSavedTerm;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedTermRepository extends JpaRepository<UserSavedTerm, Long> {
    List<UserSavedTerm> findAllByUser(User user);
    Optional<UserSavedTerm> findByUserAndTerm(User user, FinancialTerm term);
}