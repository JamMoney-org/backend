package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.stock.dto.HoldingStockResponseDto;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.stockApp.stock.entity.UserPortfolio;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.repository.UserPortfolioRepository;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPortfolioService {
    private final UserRepository userRepository;
    private final UserPortfolioRepository userPortfolioRepository;
    private final HoldingStockService holdingStockService;
    private final StockMapper stockMapper;

    // 바깥은 트랜잭션 없음 → 유저별 짧은 트랜잭션
    public void updateAllUserPortfolios() {
        int page = 0;
        int size = 500; // 배치 단위
        Page<User> slice;

        do {
            slice = userRepository.findAllWithCash(PageRequest.of(page, size));
            slice.forEach(this::updateUserPortfolioSafely);
            page++;
        } while (!slice.isEmpty());
    }

    private void updateUserPortfolioSafely(User user) {
        try {
            updateUserPortfolio(user);
        } catch (Exception e) {
            // 로깅 후 계속
        }
    }

    @Transactional
    public void updateUserPortfolio(User user) {
        // nul-safe cash
        long cash = Optional.ofNullable(user.getCash())
                .map(c -> Optional.of(c.getMoney()).orElse(0L))
                .orElse(0L);

        // 보유 주식 한번에 조회 + DTO 매핑
        List<HoldingStock> holdings = holdingStockService.getUserHoldingStocks(user.getId());
        List<HoldingStockResponseDto> dtos = stockMapper.holdingStocksToDto(holdings);

        long stockAsset = dtos.stream().mapToLong(HoldingStockResponseDto::getEvaluationAmount).sum();
        long invested = dtos.stream().mapToLong(HoldingStockResponseDto::getTotalPrice).sum();

        long totalAsset = Math.addExact(cash, stockAsset); // overflow 체크
        long profitAmount = stockAsset - invested;

        double profitRate = invested > 0
                ? BigDecimal.valueOf(profitAmount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(invested), 4, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        UserPortfolio portfolio =  userPortfolioRepository.findByUser(user);

        portfolio.setStockAsset(stockAsset);
        portfolio.setTotalAsset(totalAsset);
        portfolio.setProfitAmount(profitAmount);
        portfolio.setProfitRate(profitRate);
    }

    @Transactional(readOnly = true)
    public UserPortfolio getPortfolio(User user) {
        return userPortfolioRepository.findByUser(user);
    }
}

