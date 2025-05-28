package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.stockApp.stock.dto.HoldingStockResponseDto;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.service.HoldingStockService;
import com.example.jammoney.stockApp.stock.service.InterestingStockService;
import com.example.jammoney.user.entity.User;
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
    private final HoldingStockService holdingStockService;
    private final StockMapper stockMapper;

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
        List<HoldingStock> holdingStocks = holdingStockService.getUserHoldingStocks(customUserDetails.getUser().getId());
        List<HoldingStockResponseDto> holdingStockResponseDtos = stockMapper.holdingStocksToDto(holdingStocks);
        return new ResponseEntity<>(holdingStockResponseDtos, HttpStatus.OK);
    }

    //관심 주식 삭제
    @DeleteMapping
    public ResponseEntity deleteInterestingStock(@RequestParam long companyId,
                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        interestingStockService.deleteInterestingStock(customUserDetails.getUser(), companyId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
