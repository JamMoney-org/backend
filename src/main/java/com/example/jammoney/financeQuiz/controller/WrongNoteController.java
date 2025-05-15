package com.example.jammoney.financeQuiz.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.exception.ApiResponse;
import com.example.jammoney.financeQuiz.dto.*;
import com.example.jammoney.financeQuiz.service.QuizService;
import com.example.jammoney.financeQuiz.service.WrongNoteService;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/wrong-notes")
@RequiredArgsConstructor
public class WrongNoteController {

    private final WrongNoteService wrongNoteService;

    // 1. 오답노트 저장
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveWrongNote(
            @RequestBody WrongNoteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        wrongNoteService.saveWrongNote(request, user);
        return ResponseEntity.ok(ApiResponse.success("오답노트 저장 성공", null));
    }

    // 2. 오답노트 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<WrongNoteResponse>>> getWrongNotes(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        List<WrongNoteResponse> notes = wrongNoteService.getWrongNotesByUser(user);
        return ResponseEntity.ok(ApiResponse.success("오답노트 조회 성공", notes));
    }

    // 3. 오답노트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWrongNote(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        wrongNoteService.deleteWrongNote(id, user);
        return ResponseEntity.ok(ApiResponse.success("오답노트 삭제 완료", null));
    }
}