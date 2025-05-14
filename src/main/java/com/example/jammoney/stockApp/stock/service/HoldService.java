package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.stock.dto.HoldingStockResponseDto;
import com.example.jammoney.stockApp.stock.entity.Cash;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.stockApp.stock.repository.CashRepository;
import com.example.jammoney.stockApp.stock.repository.HoldingStockRepository;
import com.example.jammoney.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HoldService {

    private final CashRepository cashRepository;
    private final HoldingStockRepository holdingStockRepository;
    private final ApiCallService apiCallService;

    @Transactional
    public void decreaseCash(User user, long amount) {
        Cash cash = cashRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("현금 정보가 존재하지 않습니다."));
        if (cash.getMoney() < amount) {
            throw new RuntimeException("잔액이 부족합니다.");
        }
        cash.setMoney(cash.getMoney() - amount);
        cashRepository.save(cash);
    }

    @Transactional
    public void increaseCash(User user, long amount) {
        Cash cash = cashRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("현금 정보가 존재하지 않습니다."));
        cash.setMoney(cash.getMoney() + amount);
        cashRepository.save(cash);
    }

    @Transactional
    public void increaseHolding(User user, Company company, int count, long totalAmount) {
        HoldingStock holding = holdingStockRepository.findByUserAndCompany(user, company)
                .orElse(new HoldingStock(user, company, 0, 0));
        holding.setStockCount(holding.getStockCount() + count);
        holding.setTotalPrice(holding.getTotalPrice() + totalAmount);
        holdingStockRepository.save(holding);
    }

    @Transactional
    public void decreaseHolding(User user, Company company, int count) {
        HoldingStock holding = holdingStockRepository.findByUserAndCompany(user, company)
                .orElseThrow(() -> new RuntimeException("보유한 주식이 없습니다."));
        if (holding.getStockCount() < count) {
            throw new RuntimeException("보유 수량이 부족합니다.");
        }

        long avgPrice = holding.getTotalPrice() / holding.getStockCount();
        long minusAmount = avgPrice * count;

        holding.setStockCount(holding.getStockCount() - count);
        holding.setTotalPrice(holding.getTotalPrice() - minusAmount);
        holdingStockRepository.save(holding);
    }
    @Transactional
    public List<HoldingStockResponseDto> getHoldingStocks(User user) {
        List<HoldingStock> holdings = holdingStockRepository.findByUser(user);
        List<HoldingStockResponseDto> result = new ArrayList<>();

        for (HoldingStock holding : holdings) {
            String code = holding.getCompany().getCode();
            Object raw = apiCallService.getCurrentPrice(code);

            if (raw instanceof Map<?, ?> outer) {
                Map<String, String> output = (Map<String, String>) outer.get("output");
                long currentPrice = Long.parseLong(output.get("stck_prpr"));

                int count = holding.getStockCount();
                long totalBuyPrice = holding.getTotalPrice();

                long evaluationAmount = currentPrice * count;
                long profitAmount = evaluationAmount - totalBuyPrice;
                double profitRate = totalBuyPrice == 0 ? 0.0 : ((double) profitAmount / totalBuyPrice) * 100;

                result.add(HoldingStockResponseDto.builder()
                        .companyKorName(holding.getCompany().getKorName())
                        .stockCount(count)
                        .currentPrice(currentPrice)
                        .evaluationAmount(evaluationAmount)
                        .profitAmount(profitAmount)
                        .profitRate(profitRate)
                        .portfolioRatio(0.0)
                        .build());
            }
        }

        return result;
    }
}
