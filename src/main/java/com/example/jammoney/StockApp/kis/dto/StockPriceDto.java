package com.example.jammoney.StockApp.kis.dto;
import lombok.Data;

@Data
public class StockPriceDto {
    private Output output;

    @Data
    public static class Output {
        private String stck_prpr; // 현재가
        private String prdy_vrss; // 전일 대비
        private String prdy_vrss_sign; // 등락 부호
        private String prdy_ctrt; // 등락률
        private String acml_tr_pbmn; // 누적 거래대금
        private String acml_vol; // 누적 거래량
        private String stck_oprc; // 시가
        private String stck_hgpr; // 고가
        private String stck_lwpr; // 저가
        private String wghn_avrg_stck_prc; // 가중 평균 가격
        private String hts_frgn_ehrt; // 외국인 보유 비율
        private String frgn_ntby_qty; // 외국인 순매수 수량
        private String per;
        private String pbr;
        private String eps;
        private String bps;
        private String stck_shrn_iscd; // 종목코드
    }
}

