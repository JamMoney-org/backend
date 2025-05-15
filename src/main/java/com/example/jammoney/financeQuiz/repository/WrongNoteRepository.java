package com.example.jammoney.financeQuiz.repository;

import com.example.jammoney.financeQuiz.entity.FinanceQuiz;
import com.example.jammoney.financeQuiz.entity.WrongNote;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.jammoney.user.entity.User;


import java.util.List;
import java.util.Optional;

public interface WrongNoteRepository extends JpaRepository<WrongNote, Long> {

    List<WrongNote> findByUser(User user);

    Optional<WrongNote> findByUserAndQuiz(User user, FinanceQuiz quiz); // 중복 저장 방지
}