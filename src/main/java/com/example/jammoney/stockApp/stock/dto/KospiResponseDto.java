package com.example.jammoney.stockApp.stock.dto;

import lombok.Data;
/**
 * KOSPI 월봉 정보 응답 DTO
 * 주식 차트(월봉) 조회 시 클라이언트에 전달되는 데이터 구조
 */
@Data
public class KospiResponseDto {
    //현재 달 (YYYYMM)
    private String date;

    //시가
    private double open;

    //고가
    private double high;

    //저가
    private double low;

    //종가
    private double close;
}
