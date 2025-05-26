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
import java.util.*;
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

        for (Company company : companyList) {
            StockMinDto stockMinDto = apiCallService.getStockMin(company.getCode(), strHour);

            List<StockMin> stockMinList = Optional.ofNullable(stockMinDto.getOutput2())
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(dto -> dto.getStck_cntg_hour() != null)
                    .map(dto -> {
                        StockMin stockMin = apiMapper.stockMinOutput2ToStockMin(dto);
                        stockMin.setCompany(company);
                        stockMin.setTradeTime(now);
                        return stockMin;
                    })
                    .toList();

            for (StockMin stockMin : stockMinList) {
                stockMinRepository.insertIgnore(
                        stockMin.getCompany().getCompanyId(),
                        stockMin.getStockTradeTime(),
                        stockMin.getStck_cntg_hour(),
                        stockMin.getStck_prpr(),
                        stockMin.getStck_oprc(),
                        stockMin.getStck_hgpr(),
                        stockMin.getStck_lwpr(),
                        stockMin.getCntg_vol()
                );
            }

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
