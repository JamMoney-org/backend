package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.kis.dto.StockAskingPriceDto;
import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.krx.KrxDividendService;
import com.example.jammoney.stockApp.stock.dto.StockMetaDataResponseDto;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import com.example.jammoney.stockApp.stock.mapper.ApiMapper;
import com.example.jammoney.stockApp.stock.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final ApiCallService apiCallService;
    private final KrxDividendService krxDividendService;
    private final ApiMapper apiMapper;

    public Company findCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회사가 존재하지 않습니다."));
    }

    public Company findCompanyByCode(String stockCode) {
        Company company = companyRepository.findByCode(stockCode);
        return company;
    }

    public List<Company> findAllCompanies() {
        return companyRepository.findAll();
    }
    @Transactional
    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }
    public void fillCompaniesData() throws InterruptedException {
        List<String> korNames = List.of(
                "삼성전자", "LG에너지솔루션", "SK하이닉스", "삼성바이오로직스", "현대차", "NAVER",
                "POSCO홀딩스", "삼성SDI", "카카오", "LG화학", "기아", "현대모비스", "셀트리온", "삼성물산", "SK이노베이션"
        );
        List<String> codes = List.of(
                "005930", "373220", "000660", "207940", "005380", "035420",
                "005490", "006400", "035720", "051910", "000270", "012330",
                "068270", "028260", "096770"
        );

        Map<String, LocalDate> listedDates = Map.ofEntries(
                Map.entry("005930", LocalDate.of(1975, 6, 11)),
                Map.entry("373220", LocalDate.of(2022, 1, 27)),
                Map.entry("000660", LocalDate.of(1996, 12, 26)),
                Map.entry("207940", LocalDate.of(2016, 11, 10)),
                Map.entry("005380", LocalDate.of(1974, 6, 28)),
                Map.entry("035420", LocalDate.of(2008, 11, 28)),
                Map.entry("005490", LocalDate.of(1988, 6, 10)),
                Map.entry("006400", LocalDate.of(1979, 1, 29)),
                Map.entry("035720", LocalDate.of(2010, 10, 14)),
                Map.entry("051910", LocalDate.of(1970, 3, 27)),
                Map.entry("000270", LocalDate.of(1973, 7, 3)),
                Map.entry("012330", LocalDate.of(1977, 12, 22)),
                Map.entry("068270", LocalDate.of(2008, 2, 25)),
                Map.entry("028260", LocalDate.of(1975, 6, 11)),
                Map.entry("096770", LocalDate.of(2011, 1, 3))
        );

        Map<String, String> industries = Map.ofEntries(
                Map.entry("005930", "전기전자"),
                Map.entry("373220", "전기전자"),
                Map.entry("000660", "전기전자"),
                Map.entry("207940", "의약품"),
                Map.entry("005380", "운수장비"),
                Map.entry("035420", "서비스업"),
                Map.entry("005490", "철강금속"),
                Map.entry("006400", "전기전자"),
                Map.entry("035720", "서비스업"),
                Map.entry("051910", "화학"),
                Map.entry("000270", "운수장비"),
                Map.entry("012330", "운수장비"),
                Map.entry("068270", "의약품"),
                Map.entry("028260", "건설업"),
                Map.entry("096770", "화학")
        );


        List<String> failedCodes = new ArrayList<>();

        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);
            String korName = korNames.get(i);

            try {
                if (companyRepository.existsByCode(code)) {
                    log.info("이미 등록된 회사: {}", code);
                    continue;
                }

                Company company = new Company();
                company.setCode(code);
                company.setKorName(korName);
                company.setListedDate(listedDates.get(code));
                company.setIndustry(industries.get(code));

                try {
                    StockAskingPriceDto dto = apiCallService.getStockAskingPrice(code);
                    if (dto != null && dto.getOutput1() != null) {
                        StockAskingPrice askingPrice = apiMapper.toStockAskingPrice(dto.getOutput1());
                        company.setStockAskingPrice(askingPrice);
                        askingPrice.setCompany(company);
                    } else {
                        log.warn("호가 정보 수신 실패: {}", code);
                    }
                } catch (Exception e) {
                    log.warn("호가 정보 요청 실패: {}", code);
                }

                companyRepository.save(company);
                log.info("회사 저장 완료: {} ({})", korName, code);

                Thread.sleep(500);

            } catch (Exception e) {
                log.error("회사 초기화 실패: {} ({}) - {}", korName, code, e.getMessage(), e);
                failedCodes.add(code);
            }
        }

        if (!failedCodes.isEmpty()) {
            log.warn("초기화 실패 종목 목록: {}", failedCodes);
        } else {
            log.info("모든 회사 초기화 완료");
        }
    }

    @Transactional
    public void updateCompanyMeta(Company company) throws InterruptedException {
        StockMetaDataResponseDto metaData = apiCallService.getStockMetaData(company.getCode());

        company.setMarketCap(parseLong(metaData.getHts_avls()));
        company.setFaceValue(parseLong(metaData.getStck_fcam()));
        company.setListedShares(parseLong(metaData.getLstn_stcn()));
        company.setIndustry(metaData.getBscp_kor_isnm());
        company.setEps(parseBigDecimal(metaData.getEps()));
        company.setPer(parseBigDecimal(metaData.getPer()));
        company.setBps(parseBigDecimal(metaData.getBps()));
        company.setPbr(parseBigDecimal(metaData.getPbr()));
        company.setSettlementMonth(metaData.getStac_month());

        companyRepository.save(company);
        log.info("Company 정보 업데이트 완료 - 코드: {}", company.getCode());
        Thread.sleep(500);
    }

    @Transactional
    public void updateAllCompanyMeta() {
        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            try {
                updateCompanyMeta(company);
            } catch (Exception e) {
                log.error("Company 업데이트 실패 - 코드: {}", company.getCode(), e);
            }
        }
    }

    private Long parseLong(String s) {
        try { return (s != null && !s.isBlank()) ? Long.parseLong(s) : null; } catch (Exception e) { return null; }
    }

    private BigDecimal parseBigDecimal(String s) {
        try { return (s != null && !s.isBlank()) ? new BigDecimal(s) : null; } catch (Exception e) { return null; }
    }
    @Transactional
    public void updateDividendInfo() {
        List<KrxDividendService.DividendData> dividendDataList = krxDividendService.fetchDividendData();
        for (KrxDividendService.DividendData data : dividendDataList) {
            Company company = companyRepository.findByCode(data.code());
            if(company!=null){
                company.setDividendYield(data.dividendYield());
                company.setDividendPerShare(data.dividendPerShare());
                companyRepository.save(company);
                log.info("배당 수익률, 주당배당금 갱신 완료! 종목 코드: {}", data.code());
                continue;
            }
        }
    }
}
