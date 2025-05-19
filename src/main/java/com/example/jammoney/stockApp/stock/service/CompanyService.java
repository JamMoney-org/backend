package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.kis.dto.StockAskingPriceDto;
import com.example.jammoney.stockApp.stock.mapper.ApiMapper;
import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import com.example.jammoney.stockApp.stock.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        List<String> korName = List.of("삼성전자", "LG에너지솔루션", "SK하이닉스", "삼성바이오로직스", "현대차", "NAVER", "POSCO홀딩스", "삼성SDI", "카카오", "LG화학", "기아", "현대모비스", "셀트리온", "삼성물산", "SK이노베이션");
        List<String> code = List.of("005930", "373220", "000660", "207940", "005380", "035420", "005490", "006400", "035720", "051910", "000270", "012330", "068270", "028260", "096770");

        for (int i = 0; i < code.size(); i++) {
            Company company = new Company();
            company.setCode(code.get(i));
            company.setKorName(korName.get(i));
            company.setStockAskingPrice(new StockAskingPrice());

            StockAskingPriceDto stockAskingPriceDto = apiCallService.getStockAskingPrice(company.getCode());
            StockAskingPrice stockAskingPrice = apiMapper.toStockAskingPrice(stockAskingPriceDto.getOutput());

            company.setStockAskingPrice(stockAskingPrice);
            stockAskingPrice.setCompany(company);
            Thread.sleep(500);

            companyRepository.save(company);
        }
    }
}
