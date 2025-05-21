package com.example.jammoney.stockApp.stock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Company{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    private String code;

    private String korName;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private StockAskingPrice stockAskingPrice;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private StockInfo stockInfo;


}

