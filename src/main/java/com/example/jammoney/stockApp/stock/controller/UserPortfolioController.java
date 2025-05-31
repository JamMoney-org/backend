package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.stockApp.stock.dto.UserPortfolioResponseDto;
import com.example.jammoney.stockApp.stock.entity.UserPortfolio;
import com.example.jammoney.stockApp.stock.service.UserPortfolioService;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio")
public class UserPortfolioController {
    private final UserPortfolioService userPortfolioService;

    //user의 포트폴리오 현황 조회
    @GetMapping
    public ResponseEntity getMyPortfolio(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        UserPortfolio portfolio = userPortfolioService.getPortfolio(user);
        UserPortfolioResponseDto userPortfolioResponseDto;
        userPortfolioResponseDto = UserPortfolioResponseDto.builder()
                .nickname(user.getNickname())
                .money(portfolio.getCash().getMoney())
                .stockAsset(portfolio.getStockAsset())
                .totalAsset(portfolio.getTotalAsset())
                .profitAmount(portfolio.getProfitAmount())
                .profitRate(portfolio.getProfitRate())
                .build();
        return new ResponseEntity<>(userPortfolioResponseDto, HttpStatus.OK);
    }
}
