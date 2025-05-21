package com.example.jammoney.stockApp.kis.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * KOSPI 지수 일별 데이터 응답 DTO
 * - date: 날짜
 * - close: 종가 (지수)
 * - prdyVrss: 전일 대비 변동값 (포인트)
 * - prdyCrt: 전일 대비 변동률 (%)
 * - KOSPI 지수 추이 차트나 수익률 비교에 사용
 */
@Data
public class KospiDto {
    private Object output1;
    private List<KospiRawItem> output2;

    @Data
    public static class KospiRawItem {
        private String stck_bsop_date;    // 날짜
        private String bstp_nmix_oprc;    // 시가
        private String bstp_nmix_hgpr;    // 고가
        private String bstp_nmix_lwpr;    // 저가
        private String bstp_nmix_prpr;    // 종가
    }
}
