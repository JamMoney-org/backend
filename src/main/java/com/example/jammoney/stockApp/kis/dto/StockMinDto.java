package com.example.jammoney.stockApp.kis.dto;
import lombok.Data;
import java.util.List;

@Data
/**
 * [KIS API] 주식 당일 분봉 데이터 응답 DTO
 * - output2: 시간별 분봉 정보 리스트
 * - ChartData: 시가, 고가, 저가, 종가, 거래량 포함
 * - 분봉 차트 구성 시 사용 (예: 1분봉, 3분봉 차트)
 */

public class StockMinDto {
    private List<ChartData> output2;

    @Data
    public static class ChartData {
        private String stck_cntg_hour;   // 체결시간
        private String stck_prpr;        // 현재가
        private String stck_oprc;        // 시가
        private String stck_hgpr;        // 고가
        private String stck_lwpr;        // 저가
        private String acml_vol;         // 거래량
    }
}
