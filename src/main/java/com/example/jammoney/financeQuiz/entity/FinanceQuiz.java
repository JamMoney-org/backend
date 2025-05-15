package com.example.jammoney.financeQuiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "finance_quiz")
public class FinanceQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 문제 내용
    @Column(nullable = false, length = 500)
    private String question;

    // 정답 위치 (보기 인덱스 기준)
    @Column(nullable = false)
    private int correctIndex;

    @ElementCollection
    @OrderColumn(name = "option_order")
    @CollectionTable(name = "finance_quiz_options", joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "option")
    private List<String> options = new ArrayList<>();

    // 힌트
    private String hint;

    // 해설
    @Column(length = 1000)
    private String explanation;

    // 난이도 (초급~고급)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    // 카테고리 (소비, 저축 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizCategory category;

    // 생성일시
    @CreationTimestamp
    private LocalDateTime createdAt;
}