package com.example.jammoney.stockApp.stock.mapper;

import com.example.jammoney.stockApp.kis.dto.StockAskingPriceDto;
import com.example.jammoney.stockApp.kis.dto.StockMinDto;
import com.example.jammoney.stockApp.stock.dto.StockInfoResponseDto;
import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import com.example.jammoney.stockApp.stock.entity.StockInfo;
import com.example.jammoney.stockApp.stock.entity.StockMin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiMapper {
    StockAskingPrice toStockAskingPrice(StockAskingPriceDto.Output output);
    StockMin stockMinOutput2ToStockMin(StockMinDto.StockMinOutput2 stock);
    StockInfo stockMinOutput1ToStockInfo(StockMinDto.StockMinOutput1 stock);
}
