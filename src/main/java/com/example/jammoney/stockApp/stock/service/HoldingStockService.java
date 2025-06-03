package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.exception.ErrorCode;
import com.example.jammoney.exception.StockLogicException;
import com.example.jammoney.stockApp.stock.dto.HoldingStockResponseDto;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.repository.CompanyRepository;
import com.example.jammoney.stockApp.stock.repository.HoldingStockRepository;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingStockService {

    private final CompanyRepository companyRepository;
    private final HoldingStockRepository holdingStockRepository;
    private final UserRepository userRepository;
    private final StockMapper stockMapper;

    public List<HoldingStockResponseDto> setPercentage(List<HoldingStockResponseDto> holdingStockResponseDtos) {
        long totalPortfolioValue = 0L; // 전체 포트폴리오 평가금액(=총 평가금액)

        // 1. 각 종목의 평가금액, 수익금, 수익률 계산
        for (HoldingStockResponseDto dto : holdingStockResponseDtos) {
            long currentPrice = dto.getCurrentPrice();
            int totalStockCount = dto.getStockCount() + dto.getReserveSellStockCount();

            // 평가금액 = 현재가 * (보유수량 + 예약매도)
            double evaluationAmount = (double) currentPrice * totalStockCount;

            // 수익금 = 평가금액 - 총 투자금
            double profitAmount = evaluationAmount - dto.getTotalPrice();

            // 수익률 = (수익금 / 총 투자금) * 100
            double profitRate = dto.getTotalPrice() > 0
                    ? (profitAmount / dto.getTotalPrice()) * 100
                    : 0.0;

            dto.setEvaluationAmount((long) evaluationAmount);
            dto.setProfitAmount((long) profitAmount);
            dto.setProfitRate(profitRate);

            totalPortfolioValue += (long) evaluationAmount;
        }

        // 2. 포트폴리오 비율 계산 = (종목 평가금액 / 전체 평가금액) * 100
        for (HoldingStockResponseDto dto : holdingStockResponseDtos) {
            double portfolioRatio = totalPortfolioValue > 0
                    ? ((double) dto.getEvaluationAmount() / totalPortfolioValue) * 100
                    : 0.0;
            dto.setPortfolioRatio(portfolioRatio);
        }

        return holdingStockResponseDtos;
    }


    public void deleteAllHoldingStocks(long userId) {
        List<HoldingStock> holdingStocks = getUserHoldingStocks(userId);

        holdingStockRepository.deleteAll(holdingStocks);
    }

    public List<HoldingStock> getUserHoldingStocks(long userId) {

        return holdingStockRepository.findByUser(userId);
    }

    public HoldingStock getOrCreateHoldingStock(Long companyId, Long userId) {
        HoldingStock holdingStock = holdingStockRepository.findByCompanyAndUser(companyId, userId);
        if(holdingStock == null) {
            HoldingStock newHoldingStock = new HoldingStock();
            newHoldingStock.setUser(userRepository.findById(userId).orElseThrow());
            newHoldingStock.setCompany(companyRepository.findById(companyId).orElseThrow());
            newHoldingStock.setStockCount(0);
            newHoldingStock.setReserveStockCount(0);
            newHoldingStock.setTotalPrice(0);
            return holdingStockRepository.save(newHoldingStock);
        }
        return holdingStock;
    }

    public HoldingStock findHoldingStock(Long companyId, Long userId) {
        HoldingStock holdingStock = holdingStockRepository.findByCompanyAndUser(companyId, userId);
        if(holdingStock == null)
            throw new StockLogicException(ErrorCode.HOLDING_STOCK_NOT_FOUND);
        else
            return holdingStock;
    }
    public List<HoldingStockResponseDto> findHoldingStocks(long userId) {
        List<HoldingStock> holdingStocks = holdingStockRepository.findByUser(userId);

        return stockMapper.holdingStocksToDto(holdingStocks);
    }
}
