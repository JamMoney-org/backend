package com.example.jammoney.stockApp.stock.event;

import com.example.jammoney.stockApp.stock.controller.LongPollingController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderChangedEventListener {

    @EventListener
    public void onOrderChanged(OrderChangedEvent event) {
        LongPollingController.sendUpdate(
                event.getUserId(),
                event.getBuyOrders(),
                event.getSellOrders()
        );
    }
}