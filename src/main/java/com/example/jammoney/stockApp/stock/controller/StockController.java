package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.stock.dto.KospiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StockController {

    private final ApiCallService apiCallService;

    @GetMapping("/kospi")
    public ResponseEntity getKospiMonth() {
        List<KospiResponseDto> kospiResponseDtos = apiCallService.getKospiMonthlyIndexThisYear();
        return ResponseEntity.ok(kospiResponseDtos);
    }
}
