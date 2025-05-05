package com.example.jammoney.financeQuiz;

import com.example.jammoney.scenarioQuiz.Difficulty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "finance_quiz")
public class FinanceQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예: "적금과 예금의 차이점은?"
    @Column(nullable = false, length = 500)
    private String question;

    // 정답 텍스트 (보기 중 하나와 일치)
    @Column(nullable = false)
    private String correctAnswer;

    // 오답 보기들 (최대 3개)
    @ElementCollection
    @CollectionTable(name = "finance_quiz_options", joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "option")
    private List<String> options = new ArrayList<>();

    // 해설 (선택적)
    @Column(length = 1000)
    private String explanation;

    // 난이도: 1~5 (1=초급, 5=고급)
    @Column(nullable = false)
    private Difficulty difficulty;

    // 카테고리: 소비, 저축, 대출, 투자 등
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizCategory category;

    // 생성일자
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 사용자 즐겨찾기 연관
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizBookmark> bookmarkedByUsers = new ArrayList<>();
}