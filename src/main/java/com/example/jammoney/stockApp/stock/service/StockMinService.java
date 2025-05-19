package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.kis.dto.StockMinDto;
import com.example.jammoney.stockApp.stock.mapper.ApiMapper;
import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.StockInfo;
import com.example.jammoney.stockApp.stock.entity.StockMin;
import com.example.jammoney.stockApp.stock.repository.StockMinRepository;
import com.example.jammoney.util.Time;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMinService {
    private final ApiCallService apiCallService;
    private final CompanyService companyService;
    private final StockMinRepository stockMinRepository;
    private final ApiMapper apiMapper;

    public void updateStockMin() throws InterruptedException {
        List<Company> companyList = companyService.findAllCompanies();
        LocalDateTime now = LocalDateTime.now();
        String strHour = Time.strHour(now);
        for(int i = 0; i < companyList.size(); i++) {
            // 주식 코드로 회사 불러오기
            Company company = companyService.findCompanyByCode(companyList.get(i).getCode());
            // 분봉 api 호출하기
            StockMinDto stockMinDto = apiCallService.getStockMin(company.getCode(), strHour);
            // mapper로 정리 된 값 받기
            List<StockMin> stockMinList = stockMinDto.getOutput2().stream()
                    .map(stockMinOutput2 -> {
                        StockMin stockMin = apiMapper.stockMinOutput2ToStockMin(stockMinOutput2);
                        stockMin.setCompany(company);
                        stockMin.setTradeTime(now);
                        return stockMin;
                    }).collect(Collectors.toList());
            // 빠른 시간 순으로 정렬
            Collections.sort(stockMinList, Comparator.comparing(StockMin::getStockTradeTime));
            // 회사 정보 저장
            StockInfo stockInfo = apiMapper.stockMinOutput1ToStockInfo(stockMinDto.getOutput1());
            stockInfo.setCompany(company);
            StockInfo oldStockInf = company.getStockInfo();
            stockInfo.setStockInfoId(oldStockInf.getStockInfoId());
            company.setStockInfo(stockInfo);

            // 저장한다
            stockMinRepository.saveAll(stockMinList);
            companyService.saveCompany(company);


            Thread.sleep(500);
        }
    }
}
