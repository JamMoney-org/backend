// src/main/java/com/example/jammoney/news/controller/EasyWordController.java
package com.example.jammoney.news.controller;

import com.example.jammoney.news.dto.EasyWordTranslationDto;
import com.example.jammoney.news.service.EasyWordTranslationService;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news/{newsId}/easy-words")
@RequiredArgsConstructor
public class EasyWordController {

    private final EasyWordTranslationService translationService;
    private final UserRepository             userRepo;

    //1) 쉬운말 번역 + 예문 생성
    @GetMapping
    public ResponseEntity<List<EasyWordTranslationDto>> getEasyWords(
            @PathVariable Long newsId
    ) throws Exception {
        List<EasyWordTranslationDto> result = translationService.generateTranslations(newsId);
        return ResponseEntity.ok(result);
    }

    // 2) 단어장에 추가
    @PostMapping
    public ResponseEntity<Void> saveSingleTermToMyTerms(
            @PathVariable Long newsId,
            @RequestBody EasyWordTranslationDto dto,
            @AuthenticationPrincipal User user
    ) {
        translationService.saveSingleTermToMyTerms(newsId, dto, user);
        return ResponseEntity.ok().build();
    }

}
