package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.stockApp.stock.service.HoldService;
;
import com.example.jammoney.stockApp.stock.dto.OrderRequestDto;
import com.example.jammoney.stockApp.stock.dto.OrderResponseDto;
import com.example.jammoney.stockApp.stock.service.OrderService;
import com.example.jammoney.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock")
public class OrderController {
    private final OrderService orderService;
    private final HoldService holdService;

    public OrderController(OrderService orderService, HoldService holdService) {
        this.orderService = orderService;
        this.holdService = holdService;
    }
    @PostMapping("/buy")
    public ResponseEntity<OrderResponseDto> buyStock(@RequestBody OrderRequestDto dto,
                                                     @AuthenticationPrincipal User user) {
        OrderResponseDto response = orderService.placeBuyOrder(dto, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 매도 주문 API
     */
    @PostMapping("/sell")
    public ResponseEntity<OrderResponseDto> sellStock(@RequestBody OrderRequestDto dto,
                                                 @AuthenticationPrincipal User user) {
        OrderResponseDto response = orderService.placeSellOrder(dto, user);
        return ResponseEntity.ok(response);
    }
}
