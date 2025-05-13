package com.example.jammoney.financeQuiz.entity;

import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_bookmark", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "quiz_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrongNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 오답노트를 등록한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 오답노트에 등록된 문제
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private FinanceQuiz quiz;

    // 저장 시각
    @CreationTimestamp
    private LocalDateTime createdAt;

}