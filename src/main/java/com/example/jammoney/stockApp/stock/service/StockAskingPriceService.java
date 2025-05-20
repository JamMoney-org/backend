package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.exception.ErrorCode;
import com.example.jammoney.exception.StockLogicException;
import com.example.jammoney.stockApp.kis.dto.StockAskingPriceDto;
import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import com.example.jammoney.stockApp.stock.mapper.ApiMapper;
import com.example.jammoney.stockApp.stock.repository.StockAskingPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockAskingPriceService {
    private final StockAskingPriceRepository stockAskingPriceRepository;
    private final CompanyService companyService;
    private final ApiCallService apiCallService;
    private final ApiMapper apiMapper;


    public StockAskingPrice saveStockAskingPrice(StockAskingPrice stockAskingPrice) {
        return stockAskingPriceRepository.save(stockAskingPrice);
    }

    public void updateStockAskingPrice() throws InterruptedException {
        List<Company> companyList = companyService.findAllCompanies();

        for (Company company : companyList) {
            // API 호출 → Dto → Entity 변환
            StockAskingPriceDto dto = apiCallService.getStockAskingPrice(company.getCode());
            StockAskingPrice newAskingPrice = apiMapper.toStockAskingPrice(dto.getOutput1());

            // 연관관계 설정
            newAskingPrice.setCompany(company);

            // 기존 객체가 있으면 ID 복사 (update 용도)
            StockAskingPrice old = company.getStockAskingPrice();
            if (old != null) {
                newAskingPrice.setStockAskingPriceId(old.getStockAskingPriceId());
            }

            // company에 할당
            company.setStockAskingPrice(newAskingPrice);

            // 저장
            companyService.saveCompany(company);

            Thread.sleep(500);
        }
    }

    public StockAskingPrice getStockAskingPrice(Long companyId) {
        return Optional.ofNullable(stockAskingPriceRepository.findByCompany_CompanyId(companyId))
                .orElseThrow(() -> new StockLogicException(ErrorCode.ASKINGPRICE_NOT_FOUND));
    }
}
