package com.example.jammoney.news.quiz.controller;

import com.example.jammoney.news.dto.NewsQuizDto;
import com.example.jammoney.news.entity.NewsQuiz;
import com.example.jammoney.news.quiz.service.NewsQuizService;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.service.UserService;
import lombok.RequiredArgsConstructor;
import com.example.jammoney.user.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/api/news/{newsId}/quiz")
@RequiredArgsConstructor
public class NewsQuizController {
    private final UserService userService;
    private final NewsQuizService quizService;
    private final UserRepository userRepository;
    //퀴즈 생성
    @GetMapping
    public NewsQuizDto getOrCreateQuiz(@PathVariable Long newsId) {
        return quizService.getOrCreateQuiz(newsId);
    }

    //퀴즈 조회
    @PostMapping("/generate")
    public NewsQuizDto generateQuiz(@PathVariable Long newsId) {
        return quizService.generateAndSaveQuiz(newsId);
    }
    //퀴즈 제출
    @PostMapping("/submit")
    public QuizResultDto submitAnswer(
            @PathVariable Long newsId,
            @RequestBody QuizSubmissionDto submission,
            Principal principal
    ) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. email=" + email));
        return quizService.submitQuiz(newsId, submission, user);
    }

}
