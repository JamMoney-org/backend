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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
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

    // 주식 호가 정보
    @GetMapping("/{companyId}")
    public ResponseEntity getCompanyStockAsBi(@PathVariable("companyId") Long comanyId) {
        Company company = companyService.findCompanyById(comanyId);
        CompanyResponseDto companyResponseDto = stockMapper.companyToDto(company);

        return new ResponseEntity<>(companyResponseDto, HttpStatus.OK);
    }

    // 차트 하나 호출
    @GetMapping("/charts/{companyId}")
    public ResponseEntity getCompanyChart(@PathVariable("companyId") long companyId) {
        List<StockMinResponseDto> stockMinList = stockMinService.getRecent420StockMin(companyId);

        return new ResponseEntity(stockMinList, HttpStatus.OK);
    }
}
