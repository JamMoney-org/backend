package com.example.Jammoney.StockApp.kis;

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

    private final KisStockService kisStockService;

    @GetMapping("/price/{code}")
    public ResponseEntity<?> getStockPrice(@PathVariable String code) {
        return ResponseEntity.ok(kisStockService.getCurrentPrice(code));
    }
}
