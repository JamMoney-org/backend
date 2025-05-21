package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.exception.ErrorCode;
import com.example.jammoney.exception.StockLogicException;
import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.stock.dto.HoldingStockResponseDto;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.cash.repository.CashRepository;
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

    private final CashRepository cashRepository;
    private final CompanyRepository companyRepository;
    private final HoldingStockRepository holdingStockRepository;
    private final ApiCallService apiCallService;
    private final UserRepository userRepository;
    private final StockMapper stockMapper;

    public List<HoldingStockResponseDto> setPercentage(List<HoldingStockResponseDto> holdingStockResponseDtos) {
        for(HoldingStockResponseDto holdingStockResponseDto : holdingStockResponseDtos) {
            // 이름으로 회사를 불러온다
            Company company = companyRepository.findByCompanyId(holdingStockResponseDto.getCompanyId());
            // 주식 현재가를 불러온다
            String nowPrice = company.getStockInfo().getStck_prpr();
            // 주식 수익 = 전체 주식 가치 - 전체 투자 금액
            double evaluationAmount =
                    Double.parseDouble(nowPrice)
                            * (holdingStockResponseDto.getStockCount() + holdingStockResponseDto.getReserveSellStockCount());

            double totalRevenue = evaluationAmount - holdingStockResponseDto.getTotalPrice();
            double profitRate = (totalRevenue / (double)holdingStockResponseDto.getTotalPrice()) * 100;

            holdingStockResponseDto.setProfitRate(profitRate);
            holdingStockResponseDto.setEvaluationAmount((long) evaluationAmount);
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
            throw new StockLogicException(ErrorCode.HOLDINGSTOCK_NOT_FOUND);
        else
            return holdingStock;
    }
    public List<HoldingStockResponseDto> findHoldingStocks(long userId) {
        List<HoldingStock> holdingStocks = holdingStockRepository.findByUser(userId);

        return stockMapper.holdingStocksToDto(holdingStocks);
    }
}
