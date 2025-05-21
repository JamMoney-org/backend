package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.stockApp.kis.entity.Kospi;
import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.stock.dto.KospiResponseDto;
import com.example.jammoney.stockApp.stock.service.KospiService;
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

    private final KospiService kospiService;

    //kospi의 2025년 1월 ~ 현재 달까지의 월봉 정보를 저장하여 가져옴 (db에 없는 데이터만 가져옴)
    @GetMapping("/init")
    public ResponseEntity getKospiMonth() {
        List<Kospi> kospis = kospiService.saveMonthlyKospiIndex();
        return ResponseEntity.ok(kospis);
    }

    //이미 디비에 저장돼있는 정보들은 이 경로로 가져옴
    @GetMapping("/chart")
    public ResponseEntity<List<Kospi>> getChart() {
        List<Kospi> chartData = kospiService.getChart();
        return ResponseEntity.ok(chartData);
    }
}
