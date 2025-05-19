package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.stockApp.kis.service.ApiCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final ApiCallService apiCallService;

    @GetMapping("/price/{code}")
    public ResponseEntity<?> getPrice(@PathVariable String code) {
        return ResponseEntity.ok(apiCallService.getCurrentPrice(code));
    }

    @GetMapping("/askingPrice/{code}")
    public ResponseEntity<?> getAskingPrice(@PathVariable String code) {
        return ResponseEntity.ok(apiCallService.getStockAskingPrice(code));
    }

    @GetMapping("/min/{code}/{time}")
    public ResponseEntity<?> getMin(@PathVariable String code, @PathVariable String time) {
        return ResponseEntity.ok(apiCallService.getStockMin(code, time));
    }
}
