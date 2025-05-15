package com.example.jammoney.gpt;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.financeQuiz.entity.Difficulty;
import com.example.jammoney.financeQuiz.dto.FinanceQuiz;
import com.example.jammoney.financeQuiz.entity.QuizCategory;
import com.example.jammoney.gpt.service.GptApiService;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test/gpt")
@RequiredArgsConstructor
public class GptTestController {

    private final GptApiService gptApiService;

    /**
     * 인증된 사용자만 접근 가능한 GPT 퀴즈 생성 테스트 API
     */
    @GetMapping("/auth/quiz")
    public List<FinanceQuiz> testGptQuiz(
            @RequestParam QuizCategory category,
            @RequestParam Difficulty difficulty,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser(); // 필요 시 사용자 정보 활용
        return gptApiService.requestFinanceQuizzes(category, difficulty);
    }
}