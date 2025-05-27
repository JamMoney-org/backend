package com.example.jammoney.news.controller;

import com.example.jammoney.news.summary.GemmaSummaryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final GemmaSummaryClient gemmaSummaryClient;

    @PostMapping("/test")
    public ResponseEntity<String> summarizeTest(@RequestBody Map<String, String> body) {
        String input = body.get("text");
        if (input == null || input.isBlank()) {
            return ResponseEntity.badRequest().body("요약할 텍스트를 입력해주세요.");
        }

        String summary = gemmaSummaryClient.summarize(input);
        return ResponseEntity.ok(summary);
    }
}
