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
            List<HoldingStock> holdingStocks = holdingStockService.getUserHoldingStocks(user.getId());
            List<HoldingStockResponseDto> holdingStockResponseDtos = stockMapper.holdingStocksToDto(holdingStocks);
            holdingStockService.setPercentage(holdingStockResponseDtos); // 각 종목 수익률 계산

            long stockAsset = holdingStockResponseDtos.stream()
                    .mapToLong(HoldingStockResponseDto::getEvaluationAmount)
                    .sum();
            System.out.println(user.getCash().getMoney()+"!@#");
            System.out.println(stockAsset+"!@#");
            long cash = user.getCash().getMoney();
            long totalAsset = cash + stockAsset;

            long investedAmount = holdingStockResponseDtos.stream()
                    .mapToLong(HoldingStockResponseDto::getTotalPrice)
                    .sum();

            long profitAmount = totalAsset - (cash + investedAmount);
            double profitRate = investedAmount > 0 ? (profitAmount / (double) investedAmount) * 100 : 0.0;

            UserPortfolio portfolio = userPortfolioRepository.findByUser(user);

            portfolio.setCash(cash);
            portfolio.setStockAsset(stockAsset);
            portfolio.setTotalAsset(totalAsset);
            portfolio.setProfitAmount(profitAmount);
            portfolio.setProfitRate(profitRate);

            userPortfolioRepository.save(portfolio);
        }
    }

    public UserPortfolio getPortfolio(User user) {
        return userPortfolioRepository.findByUser(user);
    }
}
