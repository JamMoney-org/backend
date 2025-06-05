package com.example.jammoney.stockApp.stock.mapper;

import com.example.jammoney.stockApp.stock.dto.*;
import com.example.jammoney.stockApp.stock.entity.*;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    // 1. Company → CompanyResponseDto
    default CompanyResponseDto companyToDto(Company company) {
        if (company == null) return null;

        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setCompanyId(company.getCompanyId());
        dto.setCode(company.getCode());
        dto.setKorName(company.getKorName());
        dto.setStockAskingPriceResponseDto(stockAskingPriceToDto(company.getStockAskingPrice()));
        dto.setStockInfoResponseDto(stockInfoToDto(company.getStockInfo()));
        dto.setMarketCap(company.getMarketCap());
        dto.setFaceValue(company.getFaceValue());
        dto.setListedDate(company.getListedDate());
        dto.setListedShares(company.getListedShares());
        dto.setIndustry(company.getIndustry());
        dto.setDividendYield(company.getDividendYield());
        dto.setDividendPerShare(company.getDividendPerShare());
        dto.setEps(company.getEps());
        dto.setPbr(company.getPbr());
        dto.setBps(company.getBps());
        dto.setPer(company.getPer());
        dto.setSettlementMonth(company.getSettlementMonth());
        return dto;
    }

    default List<CompanyResponseDto> companiesToDtos(List<Company> companyList) {
        List<CompanyResponseDto> result = new ArrayList<>();
        for (Company c : companyList) result.add(companyToDto(c));
        return result;
    }

    // 2. StockInfo → StockInfoResponseDto
    default StockInfoResponseDto stockInfoToDto(StockInfo stockInfo) {
        if (stockInfo == null || stockInfo.getCompany() == null) return null;

        StockInfoResponseDto dto = new StockInfoResponseDto();
        dto.setStockInfoId(stockInfo.getStockInfoId());
        dto.setCompanyId(stockInfo.getCompany().getCompanyId());
        dto.setStck_prpr(stockInfo.getStck_prpr());
        dto.setPrdy_vrss(stockInfo.getPrdy_vrss());
        dto.setPrdy_ctrt(stockInfo.getPrdy_ctrt());
        dto.setAcml_vol(stockInfo.getAcml_vol());
        dto.setAcml_tr_pbmn(stockInfo.getAcml_tr_pbmn());
        return dto;
    }

    // 3. StockAskingPrice → StockAskingPriceResponseDto
    default StockAskingPriceResponseDto stockAskingPriceToDto(StockAskingPrice entity) {
        if (entity == null || entity.getCompany() == null) return null;

        StockAskingPriceResponseDto dto = new StockAskingPriceResponseDto();
        dto.setStockAskingPriceId(entity.getStockAskingPriceId());
        dto.setCompanyId(entity.getCompany().getCompanyId());

        // 매도 호가
        dto.setAskp1(entity.getAskp1());
        dto.setAskp2(entity.getAskp2());
        dto.setAskp3(entity.getAskp3());
        dto.setAskp4(entity.getAskp4());
        dto.setAskp5(entity.getAskp5());
        dto.setAskp6(entity.getAskp6());
        dto.setAskp7(entity.getAskp7());
        dto.setAskp8(entity.getAskp8());
        dto.setAskp9(entity.getAskp9());
        dto.setAskp10(entity.getAskp10());

        // 매도 잔량
        dto.setAskp_rsqn1(entity.getAskp_rsqn1());
        dto.setAskp_rsqn2(entity.getAskp_rsqn2());
        dto.setAskp_rsqn3(entity.getAskp_rsqn3());
        dto.setAskp_rsqn4(entity.getAskp_rsqn4());
        dto.setAskp_rsqn5(entity.getAskp_rsqn5());
        dto.setAskp_rsqn6(entity.getAskp_rsqn6());
        dto.setAskp_rsqn7(entity.getAskp_rsqn7());
        dto.setAskp_rsqn8(entity.getAskp_rsqn8());
        dto.setAskp_rsqn9(entity.getAskp_rsqn9());
        dto.setAskp_rsqn10(entity.getAskp_rsqn10());

        // 매수 호가
        dto.setBidp1(entity.getBidp1());
        dto.setBidp2(entity.getBidp2());
        dto.setBidp3(entity.getBidp3());
        dto.setBidp4(entity.getBidp4());
        dto.setBidp5(entity.getBidp5());
        dto.setBidp6(entity.getBidp6());
        dto.setBidp7(entity.getBidp7());
        dto.setBidp8(entity.getBidp8());
        dto.setBidp9(entity.getBidp9());
        dto.setBidp10(entity.getBidp10());

        // 매수 잔량
        dto.setBidp_rsqn1(entity.getBidp_rsqn1());
        dto.setBidp_rsqn2(entity.getBidp_rsqn2());
        dto.setBidp_rsqn3(entity.getBidp_rsqn3());
        dto.setBidp_rsqn4(entity.getBidp_rsqn4());
        dto.setBidp_rsqn5(entity.getBidp_rsqn5());
        dto.setBidp_rsqn6(entity.getBidp_rsqn6());
        dto.setBidp_rsqn7(entity.getBidp_rsqn7());
        dto.setBidp_rsqn8(entity.getBidp_rsqn8());
        dto.setBidp_rsqn9(entity.getBidp_rsqn9());
        dto.setBidp_rsqn10(entity.getBidp_rsqn10());

        return dto;
    }

    // 4. StockMin → StockMinResponseDto
    default StockMinResponseDto stockMinToDto(StockMin min) {
        if (min == null || min.getCompany() == null) return null;

        StockMinResponseDto dto = new StockMinResponseDto();
        dto.setCompanyId(min.getCompany().getCompanyId());
        dto.setStockMinId(min.getStockMinId());
        dto.setStockTradeTime(min.getStockTradeTime());
        dto.setStck_cntg_hour(min.getStck_cntg_hour());
        dto.setStck_prpr(min.getStck_prpr());
        dto.setStck_oprc(min.getStck_oprc());
        dto.setStck_hgpr(min.getStck_hgpr());
        dto.setStck_lwpr(min.getStck_lwpr());
        dto.setCntg_vol(min.getCntg_vol());
        return dto;
    }

    // 5. Order → OrderResponseDto
    default OrderResponseDto orderToDto(Order order) {
        if (order == null || order.getCompany() == null || order.getUser() == null) return null;

        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderId());
        dto.setCompanyKorName(order.getCompany().getKorName());
        dto.setUserId(order.getUser().getId());
        dto.setStockCount(order.getStockCount());
        dto.setPrice(order.getPrice());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setOrderType(order.getOrderType());
        dto.setModifiedAt(order.getModifiedAt());
        return dto;
    }

    default List<OrderResponseDto> ordersToDto(List<Order> orders) {
        List<OrderResponseDto> result = new ArrayList<>();
        for (Order o : orders) result.add(orderToDto(o));
        return result;
    }

    // 6. HoldingStock → HoldingStockResponseDto
    default List<HoldingStockResponseDto> holdingStocksToDto(List<HoldingStock> holdingStocks) {
        List<HoldingStockResponseDto> result = new ArrayList<>();
        for (HoldingStock holding : holdingStocks) {
            HoldingStockResponseDto dto = new HoldingStockResponseDto();
            dto.setHoldingStockId(holding.getHoldingStockId());
            dto.setCompanyId(holding.getCompany().getCompanyId());
            dto.setCompanyKorName(holding.getCompany().getKorName());
            dto.setUserId(holding.getUser().getId());
            dto.setStockCount(holding.getStockCount());
            dto.setReserveSellStockCount(holding.getReserveStockCount());
            dto.setTotalPrice(holding.getTotalPrice());
            dto.setPortfolioRatio(0.0);
            dto.setProfitRate(0D);
            dto.setProfitAmount(0);
            dto.setEvaluationAmount(0);
            dto.setCurrentPrice(Long.parseLong(holding.getCompany().getStockInfo().getStck_prpr()));
            result.add(dto);
        }
        return result;
    }

    // 7. InterestingStock → InterestingStockResponseDto
    default List<InterestingStockResponseDto> interestingStockToDto(List<InterestingStock> interestingStocks) {
        List<InterestingStockResponseDto> result = new ArrayList<>();
        for (InterestingStock star : interestingStocks) {
            InterestingStockResponseDto dto = new InterestingStockResponseDto();
            dto.setInterestingStockId(star.getInterestingStockId());
            dto.setUserId(star.getUser().getId());
            dto.setCompanyResponseDto(companyToDto(star.getCompany()));
            result.add(dto);
        }
        return result;
    }
}