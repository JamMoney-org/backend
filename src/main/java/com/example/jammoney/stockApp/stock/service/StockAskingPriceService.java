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

        for(int i = 0; i < companyList.size(); i++) {
            // 주식 코드로 회사 불러오기
            Company company = companyService.findCompanyByCode(companyList.get(i).getCode());
            // api 호출하기
            StockAskingPriceDto stockAskingPriceDto = apiCallService.getStockAskingPrice(company.getCode());
            // mapper로 정리 된 값 받기
            StockAskingPrice stockAskingPrice = apiMapper.toStockAskingPrice(stockAskingPriceDto.getOutput1());

            // 회사 등록
            stockAskingPrice.setCompany(company);
            // 호가 컬럼을 새로운 호가 컬럼으로 변경한다
            StockAskingPrice oldStockAskingPrice = company.getStockAskingPrice();
            stockAskingPrice.setStockAskingPriceId(oldStockAskingPrice.getStockAskingPriceId());
            company.setStockAskingPrice(stockAskingPrice);

            // 저장한다
            companyService.saveCompany(company);

            Thread.sleep(500);
        }
    }

    public StockAskingPrice getStockAskingPrice(Long companyId) {
        return stockAskingPriceRepository.findByCompany_CompanyId(companyId);
    }
}
