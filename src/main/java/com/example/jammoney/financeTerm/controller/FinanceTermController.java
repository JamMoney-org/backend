package com.example.jammoney.financeTerm.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.financeTerm.dto.*;
import com.example.jammoney.financeTerm.service.FinanceTermService;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class FinanceTermController {

    private final FinanceTermService financeTermService;

    //단어장의 모든 카테고리 목록을 불러옴
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(financeTermService.getAllCategories());
    }

    // 특정 카테고리 안에 존재하는 DayIndex들을 반환
    @GetMapping("/categories/{categoryName}/days")
    public ResponseEntity<List<Integer>> getDayIndexesByCategory(
            @PathVariable String categoryName
    ) {
        return ResponseEntity.ok(financeTermService.getAvailableDayIndexes(categoryName));
    }


    //특정 카테고리의 특정 DAY에 해당하는 단어 리스트 반환
    @GetMapping("/categories/{categoryName}/days/{dayIndex}/terms")
    public ResponseEntity<List<TermDto>> getTermsByCategoryAndDay(
            @PathVariable String categoryName,
            @PathVariable int dayIndex,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(
                financeTermService.getTermsByCategoryAndDay(categoryName, dayIndex, user)
        );
    }

    //DAY 단어 모두 본 뒤 퀴즈 시작 시
    @GetMapping("/quiz/batch")
    public ResponseEntity<List<TermQuizDto>> getQuizzesByCategoryAndDay(
            @RequestParam String categoryName,
            @RequestParam int dayIndex
    ) {
        return ResponseEntity.ok(financeTermService.getQuizzesByCategoryAndDay(categoryName, dayIndex));
    }

    //퀴즈 제출 → 정답 여부 확인, 학습 등록 및 경험치 반영
    @PostMapping("/quiz/submit")
    public ResponseEntity<List<QuizResultDto>> submitQuiz(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody QuizSubmitDto submitDto
    ) {
        User user = userDetails.getUser();  // 진짜 유저 엔티티 추출
        return ResponseEntity.ok(financeTermService.submitQuizAnswer(user, submitDto));
    }

    //단어 카드에서 북마크 클릭
    @PostMapping("/bookmark/{termId}")
    public ResponseEntity<Void> bookmarkTerm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long termId
    ) {
        User user = userDetails.getUser();
        financeTermService.bookmarkTerm(user, termId);
        return ResponseEntity.ok().build();
    }

    //북마크 해제
    @DeleteMapping("/bookmark/{termId}")
    public ResponseEntity<Void> unbookmarkTerm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long termId
    ) {
        User user = userDetails.getUser();
        financeTermService.unbookmarkTerm(user, termId);
        return ResponseEntity.ok().build();
    }

    //나만의 단어장 클릭
    @GetMapping("/my-terms")
    public ResponseEntity<List<UserSavedTermDto>> getSavedTerms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(financeTermService.getMySavedTerms(user));
    }

    //학습 진도율 표시용
    @GetMapping("/progress")
    public ResponseEntity<UserLearningStatusDto> getLearningStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(financeTermService.getUserLearningStatus(user));
    }

    //새로운 단어를 DB에 등록하고자 할 때
    @PostMapping
    public ResponseEntity<Void> createTerm(@RequestBody TermCreateDto dto) {
        financeTermService.createTerm(dto);
        return ResponseEntity.ok().build();
    }

}
