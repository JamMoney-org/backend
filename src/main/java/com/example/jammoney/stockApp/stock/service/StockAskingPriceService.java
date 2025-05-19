package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import com.example.jammoney.stockApp.stock.repository.StockAskingPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockAskingPriceService {
    private final StockAskingPriceRepository stockAskingPriceRepository;


    public StockAskingPrice getStockAskingPrice (Long companyId) {
        return stockAskingPriceRepository.findByCompanyCompanyId(companyId);
    }

    public boolean canMatchAsk(StockAskingPrice stockAskingPrice, long price, int quantity) {
        return matchPriceVolume(stockAskingPrice, price, quantity, true);
    }

    public boolean canMatchBid(StockAskingPrice stockAskingPrice, long price, int quantity) {
        return matchPriceVolume(stockAskingPrice, price, quantity, false);
    }

    private boolean matchPriceVolume(StockAskingPrice stockAskingPrice, long price, int quantity, boolean isAsk) {
        for (int i = 1; i <= 10; i++) {
            try {
                long p = Long.parseLong(getField(stockAskingPrice, (isAsk ? "askp" : "bidp") + i));
                int q = Integer.parseInt(getField(stockAskingPrice, (isAsk ? "askp_rsqn" : "bidp_rsqn") + i));
                if (p == price && q >= quantity) return true;
            } catch (Exception e) {
                continue;
            }
        }
        return false;
    }

    private String getField(StockAskingPrice stockAskingPrice, String fieldName) {
        try {
            String method = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
            return (String) StockAskingPrice.class.getMethod(method).invoke(stockAskingPrice);
        } catch (Exception e) {
            return "0";
        }
    }
}
