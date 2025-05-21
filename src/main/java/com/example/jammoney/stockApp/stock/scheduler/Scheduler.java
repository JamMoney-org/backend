package com.example.jammoney.stockApp.stock.scheduler;

import com.example.jammoney.stockApp.stock.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Scheduler {
    private final StockAskingPriceService stockAskingPriceService;
    private final StockMinService stockMinService;
    private final OrderService orderService;
    private final UserPortfolioService userPortfolioService;
    private final KospiService kospiService;
    @Scheduled(cron = "0 0,30 9-15 * * MON-FRI")
    public void updateAll() throws InterruptedException {

        //모든 회사의 호가 정보 갱신
        stockAskingPriceService.updateStockAskingPrice();

        //모든 회사의 미체결 주문 확인
        orderService.checkOrder();

        //모든 회사의 차트 갱신
        stockMinService.updateStockMin();

        //모든 유저의 수익률 갱신
        userPortfolioService.updateAllUserPortfolios();
    }

    @Scheduled(cron = "0 0 3 1 * *")
    public void saveKospiIndex() {
        kospiService.saveMonthlyKospiIndex();
    }
}
