package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.stock.dto.HoldingStockResponseDto;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.stockApp.stock.entity.UserPortfolio;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.repository.UserPortfolioRepository;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPortfolioService {

    private final UserRepository userRepository;
    private final UserPortfolioRepository userPortfolioRepository;
    private final HoldingStockService holdingStockService;
    private final StockMapper stockMapper;

    @Transactional
    public void updateAllUserPortfolios() {
        List<User> allUsers = userRepository.findAllWithCash();
        for (User user : allUsers) {
            updateUserPortfolio(user);
        }
    }

    @Transactional
    public void updateUserPortfolio(User user) {
        List<HoldingStock> holdingStocks = holdingStockService.getUserHoldingStocks(user.getId());
        List<HoldingStockResponseDto> holdingStockResponseDtos = stockMapper.holdingStocksToDto(holdingStocks);
        holdingStockService.setPercentage(holdingStockResponseDtos);

        long stockAsset = holdingStockResponseDtos.stream()
                .mapToLong(HoldingStockResponseDto::getEvaluationAmount)
                .sum();

        long cash = user.getCash().getMoney();
        long totalAsset = cash + stockAsset;

        long investedAmount = holdingStockResponseDtos.stream()
                .mapToLong(HoldingStockResponseDto::getTotalPrice)
                .sum();
        
        long profitAmount = stockAsset - investedAmount;
        double profitRate = investedAmount > 0 ? (profitAmount / (double) investedAmount) * 100 : 0.0;

        UserPortfolio portfolio = userPortfolioRepository.findByUser(user);
        if (portfolio == null) {
            throw new IllegalStateException("UserPortfolio is not initialized for user: " + user.getId());
        }
        portfolio.setStockAsset(stockAsset);
        portfolio.setTotalAsset(totalAsset);
        portfolio.setProfitAmount(profitAmount);
        portfolio.setProfitRate(profitRate);

        userPortfolioRepository.save(portfolio);
    }



    public UserPortfolio getPortfolio(User user) {
        return userPortfolioRepository.findByUser(user);
    }
}
