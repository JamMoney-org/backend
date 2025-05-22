package com.example.jammoney.stockApp.stock.mapper;

import com.example.jammoney.stockApp.kis.dto.StockAskingPriceDto;
import com.example.jammoney.stockApp.kis.dto.StockMinDto;
import com.example.jammoney.stockApp.stock.dto.StockInfoResponseDto;
import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import com.example.jammoney.stockApp.stock.entity.StockInfo;
import com.example.jammoney.stockApp.stock.entity.StockMin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApiMapper {
    StockAskingPrice toStockAskingPrice(StockAskingPriceDto.Output output);
//    @Mapping(source = "stck_cntg_hour", target = "stck_cntg_hour")
//    @Mapping(source = "stck_prpr", target = "stck_prpr")
//    @Mapping(source = "stck_oprc", target = "stck_oprc")
//    @Mapping(source = "stck_hgpr", target = "stck_hgpr")
//    @Mapping(source = "stck_lwpr", target = "stck_lwpr")
//    @Mapping(source = "cntg_vol", target = "cntg_vol")
    StockMin stockMinOutput2ToStockMin(StockMinDto.StockMinOutput2 stock);
    StockInfo stockMinOutput1ToStockInfo(StockMinDto.StockMinOutput1 stock);
}
