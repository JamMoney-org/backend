package com.example.jammoney.stockApp.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
/**
 * 10단계 매수/매도 호가 및 잔량 응답 DTO
 * - 매도 호가 askp1~10 / 매수 호가 bidp1~10
 * - 각각의 잔량 정보 포함 (askp_rsqn / bidp_rsqn)
 * - 실시간 호가판 구성 시 사용
 */

public class StockAskingPriceResponseDto {

    //호가정보 id
    private long stockAskingPriceId;

    //회사 id
    private long companyId;

    //매도 호가
    private String askp1;
    private String askp2;
    private String askp3;
    private String askp4;
    private String askp5;
    private String askp6;
    private String askp7;
    private String askp8;
    private String askp9;
    private String askp10;

    //매도 잔량
    private String askp_rsqn1;
    private String askp_rsqn2;
    private String askp_rsqn3;
    private String askp_rsqn4;
    private String askp_rsqn5;
    private String askp_rsqn6;
    private String askp_rsqn7;
    private String askp_rsqn8;
    private String askp_rsqn9;
    private String askp_rsqn10;

    //매수 호가
    private String bidp1;
    private String bidp2;
    private String bidp3;
    private String bidp4;
    private String bidp5;
    private String bidp6;
    private String bidp7;
    private String bidp8;
    private String bidp9;
    private String bidp10;

    //매수 잔량
    private String bidp_rsqn1;
    private String bidp_rsqn2;
    private String bidp_rsqn3;
    private String bidp_rsqn4;
    private String bidp_rsqn5;
    private String bidp_rsqn6;
    private String bidp_rsqn7;
    private String bidp_rsqn8;
    private String bidp_rsqn9;
    private String bidp_rsqn10;
}
