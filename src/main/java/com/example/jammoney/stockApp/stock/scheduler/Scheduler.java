package com.example.jammoney.stockApp.stock.scheduler;

import com.example.jammoney.stockApp.stock.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class Scheduler {
    private final StockAskingPriceService stockAskingPriceService;
    private final StockMinService stockMinService;
    private final OrderService orderService;
    private final UserPortfolioService userPortfolioService;
    private final KospiService kospiService;
    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI", zone = "Asia/Seoul")
    public void updateAll() throws InterruptedException {
        log.info("스케줄러 실행 시간: {}", LocalDateTime.now());
        //모든 회사의 호가 정보 갱신
        stockAskingPriceService.updateStockAskingPrice();
        log.info("호가 정보 갱신 끝");
        //모든 회사의 미체결 주문 확인
        orderService.checkOrder();
        log.info("미체결 주문 확인 끝");
        //모든 회사의 차트 갱신
        stockMinService.updateStockMin();
        log.info("차트 갱신 끝");
        //모든 유저의 수익률 갱신
        userPortfolioService.updateAllUserPortfolios();
        log.info("수익률 갱신 끝");
    }

    @Scheduled(cron = "0 0 3 1 * *")
    public void saveKospiIndex() {
        kospiService.saveMonthlyKospiIndex();
    }
}
