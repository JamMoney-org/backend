package com.example.jammoney.financeQuiz.entity;

import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrongNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 오답 노트를 저장한 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 문제 내용 (GPT 퀴즈 복사본)
    @Column(nullable = false)
    private String question;

    // 보기 중 사용자가 고른 보기 (문자열 그대로 저장)
    @Column(nullable = false)
    private String selectedOption;

    // 정답 보기
    @Column(nullable = false)
    private String correctAnswer;

    // 해설
    @Column(columnDefinition = "TEXT")
    private String explanation;

    // 힌트
    @Column(columnDefinition = "TEXT")
    private String hint;

    // 카테고리 (소비 / 저축 / ...)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizCategory category;
}