package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.stock.dto.KospiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kospi")
public class KospiController {

    private final ApiCallService apiCallService;

    //kospi의 2025년 1월 ~ 현재 달까지의 월봉 정보를 가져옴
    @GetMapping()
    public ResponseEntity getKospiMonth() {
        List<KospiResponseDto> kospiResponseDtos = apiCallService.getKospiMonthlyIndexThisYear();
        return ResponseEntity.ok(kospiResponseDtos);
    }
}
