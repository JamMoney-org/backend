package com.example.jammoney.stockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
/**
 * 사용자가 등록한 관심 종목 응답 DTO
 * - interestingStockId: 관심 등록 ID
 * - companyResponseDto: 해당 종목의 상세 정보
 * - 관심 종목 리스트 표시 시 사용
 */

public class InterestingStockResponseDto {
    private long interestingStockId;
    private CompanyResponseDto companyResponseDto;
}
