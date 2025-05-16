package com.example.jammoney.stockApp.kis.mapper;

import com.example.jammoney.stockApp.kis.dto.StockAskingPriceDto;
import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiMapper {
    StockAskingPrice toStockAskingPrice(StockAskingPriceDto.Output output);
}
