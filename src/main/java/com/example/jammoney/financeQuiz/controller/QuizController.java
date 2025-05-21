package com.example.jammoney.financeQuiz.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.exception.ApiResponse;
import com.example.jammoney.financeQuiz.dto.*;
import com.example.jammoney.financeQuiz.service.QuizService;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController { //금융상식퀴즈

    private final QuizService quizService;

    // 1. 퀴즈 생성 (GPT로 5개 생성)
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<FinanceQuiz>>> generateQuiz(
            @RequestBody QuizRequest request
    ) {
        List<FinanceQuiz> quizList = quizService.generateQuiz(
                request.getCategory(),
                request.getDifficulty()
        );
        return ResponseEntity.ok(ApiResponse.success("퀴즈 생성 성공", quizList));
    }

    // 2. 퀴즈 정답 제출 (1문제 채점)
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizResult>> submitAnswer(
            @RequestBody QuizSubmitRequest request
    ) {
        QuizResult result = quizService.submitAnswer(request.getQuiz(), request.getUserAnswerIndex());
        return ResponseEntity.ok(ApiResponse.success("정답 제출 결과", result));
    }

    // 3. 오답노트 저장 (틀린 문제 중 유저가 선택한 것만 저장)
    @PostMapping("/wrong-note")
    public ResponseEntity<ApiResponse<Void>> saveWrongNote(
            @RequestBody WrongNoteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        quizService.saveWrongNote(request, user);
        return ResponseEntity.ok(ApiResponse.success("오답노트 저장 완료", null));
    }

    // 4. 퀴즈 완료 (5문제 완료 → 보상 지급)
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<QuizSummaryResult>> completeQuiz(
            @RequestBody List<QuizResult> quizResults,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        QuizSummaryResult summary = quizService.submitQuizSet(quizResults, user);
        return ResponseEntity.ok(ApiResponse.success("퀴즈 완료", summary));
    }
}