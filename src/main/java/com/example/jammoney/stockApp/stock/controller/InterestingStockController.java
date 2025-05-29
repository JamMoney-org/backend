package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.stockApp.stock.dto.InterestingStockResponseDto;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.service.HoldingStockService;
import com.example.jammoney.stockApp.stock.service.InterestingStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interestingStocks")
public class InterestingStockController {
    private final InterestingStockService interestingStockService;

    //관심 주식 등록
    @PostMapping
    public ResponseEntity setInterestingStock(@RequestParam long companyId,
                                              @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        interestingStockService.saveInterestingStock(customUserDetails.getUser(), companyId);

        return new ResponseEntity(HttpStatus.CREATED);
    }

    //관심 주식들 조회
    @GetMapping
    public ResponseEntity getInterestingStockList( @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<InterestingStockResponseDto> holdingStocks = interestingStockService.getInterestingStockResponseDto(customUserDetails.getUser());
        return new ResponseEntity<>(holdingStocks, HttpStatus.OK);
    }

    //관심 주식 삭제
    @DeleteMapping
    public ResponseEntity deleteInterestingStock(@RequestParam long companyId,
                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        interestingStockService.deleteInterestingStock(customUserDetails.getUser(), companyId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
