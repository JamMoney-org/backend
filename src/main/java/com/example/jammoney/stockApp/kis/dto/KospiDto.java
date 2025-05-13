package com.example.jammoney.stockApp.kis.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
/**
 * KOSPI 지수 일별 데이터 응답 DTO
 * - date: 날짜
 * - close: 종가 (지수)
 * - prdyVrss: 전일 대비 변동값 (포인트)
 * - prdyCrt: 전일 대비 변동률 (%)
 * - KOSPI 지수 추이 차트나 수익률 비교에 사용
 */

public class KospiDto {

    private LocalDate date;
    private int close;
    private int prdyVrss;
    private double prdyCrt;
}
