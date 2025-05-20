package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.kis.dto.StockAskingPriceDto;
import com.example.jammoney.stockApp.stock.mapper.ApiMapper;
import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import com.example.jammoney.stockApp.stock.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final ApiCallService apiCallService;
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

        List<String> failedCodes = new ArrayList<>();

        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);
            String korName = korNames.get(i);

            try {
                // 중복 체크
                if (companyRepository.existsByCode(code)) {
                    log.info("이미 등록된 회사입니다: {}", code);
                    continue;
                }

                // 호가 정보 요청
                StockAskingPriceDto dto = apiCallService.getStockAskingPrice(code);
                if (dto == null || dto.getOutput1() == null) {
                    log.warn("호가 정보 수신 실패: {}", code);
                    failedCodes.add(code);
                    continue;
                }

                StockAskingPrice askingPrice = apiMapper.toStockAskingPrice(dto.getOutput1());

                // Company 및 관계 설정
                Company company = new Company();
                company.setCode(code);
                company.setKorName(korName);
                company.setStockAskingPrice(askingPrice);
                askingPrice.setCompany(company);

                companyRepository.save(company);
                log.info("회사 저장 완료: {} ({})", korName, code);

                Thread.sleep(500); // 너무 빠른 API 호출 방지

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
}
