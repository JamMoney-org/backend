package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.stockApp.stock.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/init")
@RequiredArgsConstructor
public class CompanyInitController {

    private final CompanyService companyService;


    //모의투자에서 사용할 회사의 이름과 종목코드를 받아서 DB에 저장함 (서버 실행시 딱 한 번만 실행하는 메서드)
    @GetMapping
    public ResponseEntity<Void> initialize() throws InterruptedException {
        companyService.fillCompaniesData();
        return ResponseEntity.ok().build();
    }
}