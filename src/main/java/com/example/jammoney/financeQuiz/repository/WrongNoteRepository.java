package com.example.jammoney.financeQuiz.repository;

import com.example.jammoney.financeQuiz.entity.WrongNote;
import com.example.jammoney.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WrongNoteRepository extends JpaRepository<WrongNote, Long> {

    // 특정 유저의 오답노트 전체 조회
    List<WrongNote> findAllByUser(User user);
}