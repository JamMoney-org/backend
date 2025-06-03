package com.example.jammoney.stockApp.stock.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockMetaDataResponseDto {
    private String hts_avls;         // 시가총액
    private String stck_fcam;        // 액면가
    private String lstn_stcn;        // 상장 주식 수
    private String bstp_kor_isnm;    // 업종명
    private String eps;              // EPS
    private String per;              // PER
    private String bps;              // BPS
    private String pbr;              // PBR
    private String stac_month;       // 결산월

}