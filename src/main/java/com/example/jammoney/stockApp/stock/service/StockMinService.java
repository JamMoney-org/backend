package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.kis.dto.StockMinDto;
import com.example.jammoney.stockApp.stock.dto.StockMinResponseDto;
import com.example.jammoney.stockApp.stock.mapper.ApiMapper;
import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.StockInfo;
import com.example.jammoney.stockApp.stock.entity.StockMin;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.repository.StockMinRepository;
import com.example.jammoney.util.Time;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StockMinService {
    private final ApiCallService apiCallService;
    private final CompanyService companyService;
    private final StockMinRepository stockMinRepository;
    private final ApiMapper apiMapper;
    private final StockMapper stockMapper;

    @Transactional
    public void updateStockMin() throws InterruptedException {
        List<Company> companyList = companyService.findAllCompanies();
        LocalDateTime now = LocalDateTime.now();
        String strHour = Time.strHour(now);
        int i = 0;
        for (Company company : companyList) {
            // 분봉 API 호출
            log.info("Company Code : {}", company.getCode());
            StockMinDto stockMinDto = apiCallService.getStockMin(company.getCode(), strHour);
            log.info("get api from kis complete. : {}", company.getCode());
            // 분봉 리스트 매핑 및 저장
            List<StockMin> stockMinList = stockMinDto.getOutput2().stream()
                    .filter(dto -> dto.getStck_cntg_hour() != null)  // null 방지
                    .map(stockMinOutput2 -> {
                        StockMin stockMin = apiMapper.stockMinOutput2ToStockMin(stockMinOutput2);
                        stockMin.setCompany(company);
                        stockMin.setTradeTime(now);
                        return stockMin;
                    })
                    .sorted(Comparator.comparing(StockMin::getStockTradeTime))
                    .collect(Collectors.toList());

            stockMinRepository.saveAll(stockMinList);
            log.info("complete_count : {}", i++);

            // StockInfo 생성 및 기존 ID 세팅
            StockInfo stockInfo = apiMapper.stockMinOutput1ToStockInfo(stockMinDto.getOutput1());
            stockInfo.setCompany(company);

            StockInfo oldStockInfo = company.getStockInfo();
            if (oldStockInfo != null) {
                stockInfo.setStockInfoId(oldStockInfo.getStockInfoId());  // UPDATE로 처리되게 함
            }

            company.setStockInfo(stockInfo);
            companyService.saveCompany(company);  // cascade로 stockInfo도 같이 저장

            Thread.sleep(500);
        }
        log.info("StockMin update finished");
    }
    public List<StockMin> getChart(long companyId) {

        return stockMinRepository.findAllByCompany_CompanyId(companyId);
    }

    public List<StockMinResponseDto> getRecent420StockMin(long companyId) {
        List<StockMin> stockMinList = stockMinRepository.findTop420ByCompanyIdOrderByStockMinIdDesc(companyId);

        List<StockMinResponseDto> stockMinResponseDtos = stockMinList.stream()
                .map(stockMapper::stockMinToDto).collect(Collectors.toList());
        Collections.reverse(stockMinResponseDtos);
        return stockMinResponseDtos;
    }
}
