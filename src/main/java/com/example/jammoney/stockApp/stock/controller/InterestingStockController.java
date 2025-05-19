package com.example.jammoney.stockApp.stock.controller;

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
@RequestMapping("/interestingStocks")
public class InterestingStockController {
    private final InterestingStockService interestingStockService;
    private final HoldingStockService holdingStockService;
    private final StockMapper stockMapper;

    //관심 주식 등록
    @PostMapping
    public ResponseEntity setStar(@RequestParam long companyId,
                                  @AuthenticationPrincipal User user) {
        interestingStockService.saveInterestingStock(user, companyId);

        return new ResponseEntity(HttpStatus.CREATED);
    }

    //관심 주식들 조회
    @GetMapping
    public ResponseEntity getInterestingStockList(@AuthenticationPrincipal User user) {
        List<HoldingStock> holdingStocks = holdingStockService.getUserHoldingStocks(user.getId());
        List<HoldingStockResponseDto> holdingStockResponseDtos = stockMapper.holdingStocksToDto(holdingStocks);
        return new ResponseEntity<>(holdingStockResponseDtos, HttpStatus.OK);
    }

    //관심 주식 삭제
    @DeleteMapping
    public ResponseEntity deleteInterestingStock(@RequestParam long companyId,
                                     @AuthenticationPrincipal User user) {
        interestingStockService.deleteInterestingStock(user, companyId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
