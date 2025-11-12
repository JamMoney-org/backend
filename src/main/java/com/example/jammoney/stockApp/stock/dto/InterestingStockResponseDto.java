package com.example.jammoney.stockApp.stock.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
/**
 * 사용자가 등록한 관심 종목 응답 DTO
 * - interestingStockId: 관심 등록 ID
 * - companyResponseDto: 해당 종목의 상세 정보
 * - 관심 종목 리스트 표시 시 사용
 */

public class InterestingStockResponseDto {

    //관심 주식 id
    private long interestingStockId;

    //user id
    private long userId;

    //회사 정보와 매핑
    private CompanyResponseDto companyResponseDto;
}
