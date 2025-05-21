package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.stockApp.stock.dto.CompanyResponseDto;
import com.example.jammoney.stockApp.stock.dto.StockMinResponseDto;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.service.CompanyService;
import com.example.jammoney.stockApp.stock.service.StockMinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company")
public class CompanyController {
    private final CompanyService companyService;
    private final StockMinService stockMinService;
    private final StockMapper stockMapper;
    // 전체 회사 리스트
    @GetMapping
    public ResponseEntity getCompanyList() {
        List<Company> companyList = companyService.findAllCompanies();
        List<CompanyResponseDto> companyResponseDtoList = stockMapper.companiesToDtos(companyList);

        return new ResponseEntity<>(companyResponseDtoList, HttpStatus.OK);
    }

    // 특정 회사의 주식 호가 정보
    @GetMapping("/{companyId}")
    public ResponseEntity getCompanyStockAskingPrice(@PathVariable("companyId") Long comanyId) {
        Company company = companyService.findCompanyById(comanyId);
        CompanyResponseDto companyResponseDto = stockMapper.companyToDto(company);

        return new ResponseEntity<>(companyResponseDto, HttpStatus.OK);
    }

    // 특정 회사의 최신 -> 과거 순으로 420개의 분봉 차트 가져옴 (7시간치)
    @GetMapping("/charts/{companyId}")
    public ResponseEntity getCompanyChart(@PathVariable("companyId") Long companyId) {
        List<StockMinResponseDto> stockMinList = stockMinService.getRecent420StockMin(companyId);

        return new ResponseEntity(stockMinList, HttpStatus.OK);
    }
}
