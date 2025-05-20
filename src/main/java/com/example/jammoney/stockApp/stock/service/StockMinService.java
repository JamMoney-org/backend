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

    public void updateStockMin() throws InterruptedException {
        List<Company> companyList = companyService.findAllCompanies();
        LocalDateTime tenAM = LocalDateTime.now().withHour(11).withMinute(0).withSecond(0).withNano(1);
        String strHour = Time.strHour(tenAM);

        for (Company company : companyList) {
            // 분봉 API 호출
            log.info("Company Code : " + company.getCode());
            StockMinDto stockMinDto = apiCallService.getStockMin(company.getCode(), strHour);
            log.info("Output2 size: {}", stockMinDto.getOutput2().size());
            for (StockMinDto.StockMinOutput2 o : stockMinDto.getOutput2()) {
                log.info("raw stck_cntg_hour = {}", o.getStck_cntg_hour());
            }

            // 분봉 리스트 매핑 및 저장
            List<StockMin> stockMinList = stockMinDto.getOutput2().stream()
                    .map(stockMinOutput2 -> {
                        StockMin stockMin = apiMapper.stockMinOutput2ToStockMin(stockMinOutput2);
                        stockMin.setCompany(company);
                        stockMin.setTradeTime(tenAM);
                        return stockMin;
                    })
                    .sorted(Comparator.comparing(StockMin::getStockTradeTime))
                    .collect(Collectors.toList());
            for (StockMin min : stockMinList) {
                log.info("Saving StockMin: companyId={}, time={}, price={}",
                        min.getCompany().getCompanyId(),
                        min.getStockTradeTime(),
                        min.getStck_prpr());
            }
            stockMinRepository.saveAll(stockMinList);

            // StockInfo 생성 및 연관관계 설정
            StockInfo stockInfo = apiMapper.stockMinOutput1ToStockInfo(stockMinDto.getOutput1());
            log.info("연결된 stockInfo: {}", company.getStockInfo());
            stockInfo.setCompany(company);
            company.setStockInfo(stockInfo);

            // 저장 (cascade = ALL이므로 함께 저장됨)
            companyService.saveCompany(company);

            Thread.sleep(500);
        }
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
