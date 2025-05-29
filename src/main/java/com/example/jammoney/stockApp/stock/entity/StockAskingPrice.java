package com.example.jammoney.stockApp.stock.entity;


import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
public class StockAskingPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockAskingPriceId;

    @OneToOne
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

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


    //매수 호가
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

    public String getAskp(int level) {
        return switch (level) {
            case 1 -> askp1;
            case 2 -> askp2;
            case 3 -> askp3;
            case 4 -> askp4;
            case 5 -> askp5;
            case 6 -> askp6;
            case 7 -> askp7;
            case 8 -> askp8;
            case 9 -> askp9;
            case 10 -> askp10;
            default -> null;
        };
    }

    public String getAskp_rsqn(int level) {
        return switch (level) {
            case 1 -> askp_rsqn1;
            case 2 -> askp_rsqn2;
            case 3 -> askp_rsqn3;
            case 4 -> askp_rsqn4;
            case 5 -> askp_rsqn5;
            case 6 -> askp_rsqn6;
            case 7 -> askp_rsqn7;
            case 8 -> askp_rsqn8;
            case 9 -> askp_rsqn9;
            case 10 -> askp_rsqn10;
            default -> null;
        };
    }

    public String getBidp(int level) {
        return switch (level) {
            case 1 -> bidp1;
            case 2 -> bidp2;
            case 3 -> bidp3;
            case 4 -> bidp4;
            case 5 -> bidp5;
            case 6 -> bidp6;
            case 7 -> bidp7;
            case 8 -> bidp8;
            case 9 -> bidp9;
            case 10 -> bidp10;
            default -> null;
        };
    }

    public String getBidp_rsqn(int level) {
        return switch (level) {
            case 1 -> bidp_rsqn1;
            case 2 -> bidp_rsqn2;
            case 3 -> bidp_rsqn3;
            case 4 -> bidp_rsqn4;
            case 5 -> bidp_rsqn5;
            case 6 -> bidp_rsqn6;
            case 7 -> bidp_rsqn7;
            case 8 -> bidp_rsqn8;
            case 9 -> bidp_rsqn9;
            case 10 -> bidp_rsqn10;
            default -> null;
        };
    }

}
