package com.example.Jammoney.StockApp.kis.dto;
import lombok.Data;
import java.util.List;

@Data
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
