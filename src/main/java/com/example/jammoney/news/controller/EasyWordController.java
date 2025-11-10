package com.example.jammoney.news.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.news.dto.EasyWordTranslationDto;
import com.example.jammoney.news.service.EasyWordTranslationService;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news/{newsId}/easy-words")
@RequiredArgsConstructor
@Slf4j
public class EasyWordController {

    private final EasyWordTranslationService translationService;
    private final UserRepository userRepo;

    @GetMapping
    public ResponseEntity<List<EasyWordTranslationDto>> getEasyWords(
            @PathVariable Long newsId
    ) throws Exception {
        List<EasyWordTranslationDto> result = translationService.generateTranslations(newsId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Void> saveSingleTermToMyTerms(
            @PathVariable Long newsId,
            @RequestBody EasyWordTranslationDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        log.info("[SAVE-TERM] userId={} originalWord={}", user.getId(), dto.getOriginalWord());
        translationService.saveSingleTermToMyTerms(newsId, dto, user);
        return ResponseEntity.ok().build();
    }
}
