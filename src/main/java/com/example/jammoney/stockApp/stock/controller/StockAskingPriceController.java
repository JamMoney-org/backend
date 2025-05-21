package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.stockApp.stock.service.StockAskingPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StockAskingPriceController {

    private final StockAskingPriceService stockAskingPriceService;

    @GetMapping("/update-asking-price")
    public ResponseEntity<String> updateAllAskingPrices() throws InterruptedException {
        stockAskingPriceService.updateStockAskingPrice();
        return ResponseEntity.ok("주식 호가 업데이트 완료");
    }

}
