package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.stockApp.stock.dto.HoldingStockResponseDto;
import com.example.jammoney.stockApp.stock.entity.Order;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.dto.OrderResponseDto;
import com.example.jammoney.stockApp.stock.service.HoldingStockService;
import com.example.jammoney.stockApp.stock.service.OrderService;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stock")
public class OrderController {
    private final OrderService orderService;
    private final HoldingStockService holdingStockService;
    private final StockMapper stockMapper;
    @PostMapping("/buy")
    public ResponseEntity buyStocks(@RequestParam(name = "companyId") long companyId,
                                    @RequestParam(name = "price") long price,
                                    @RequestParam(name = "stockCount") int stockCount,
                                    @AuthenticationPrincipal User user) {
        Order order = orderService.buyStocks(user, companyId, price, stockCount);
        OrderResponseDto stockOrderResponseDto = stockMapper.orderToDto(order);

        return new ResponseEntity<>(stockOrderResponseDto, HttpStatus.CREATED);
    }

    //보유중인 특정 주식 매도
    @PostMapping("/sell")
    public ResponseEntity sellStocks(@RequestParam(name = "companyId") long companyId,
                                     @RequestParam(name = "price") long price,
                                     @RequestParam(name = "stockCount") int stockCount,
                                     @AuthenticationPrincipal User user) {
        Order order = orderService.sellStocks(user, companyId, price, stockCount);
        OrderResponseDto stockOrderResponseDto = stockMapper.orderToDto(order);

        return new ResponseEntity<>(stockOrderResponseDto, HttpStatus.CREATED);
    }

    //특정 사용자의 모든 보유 주식들 조회
    @GetMapping("/holdingStocks")
    public ResponseEntity getHoldingStocks(@AuthenticationPrincipal User user) {
        List<HoldingStockResponseDto> stockHoldResponseDtos = holdingStockService.findHoldingStocks(user.getId());
        stockHoldResponseDtos = holdingStockService.setPercentage(stockHoldResponseDtos);

        return new ResponseEntity<>(stockHoldResponseDtos, HttpStatus.OK);
    }

    //특정 사용자의 모든 주문들 조회
    @GetMapping("/orders")
    public ResponseEntity getOrders(@AuthenticationPrincipal User user) {
        List<OrderResponseDto> stockOrderResponseDtos = orderService.getUserStockOrders(user.getId());

        return new ResponseEntity<>(stockOrderResponseDtos, HttpStatus.OK);
    }

    // 예약된 매수, 매도 삭제
    @DeleteMapping("/orders")
    public void deleteStockOrders(@AuthenticationPrincipal User user,
                                  @RequestParam("stockOrderId") long orderId,
                                  @RequestParam("stockCount") int stockCount) {
        orderService.deleteOrder(user, orderId, stockCount);
    }

    //예약된 주문들에 대해서 호가 확인해서 체결 처리
    @GetMapping("/checkOrder")
    public ResponseEntity checkOrder() {
        orderService.checkOrder();

        return new ResponseEntity(HttpStatus.OK);
    }
}
