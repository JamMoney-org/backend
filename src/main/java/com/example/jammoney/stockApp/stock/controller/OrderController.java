package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.stockApp.stock.dto.HoldingStockResponseDto;
import com.example.jammoney.stockApp.stock.dto.OrderResponseDto;
import com.example.jammoney.stockApp.stock.entity.Order;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
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
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    private final StockMapper stockMapper;
    private final HoldingStockService holdingStockService;

    //매수
    @PostMapping("/buy")
    public ResponseEntity buyStocks(@RequestParam(name = "companyId") long companyId,
                                    @RequestParam(name = "price") long price,
                                    @RequestParam(name = "stockCount") int stockCount,
                                    @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Order order = orderService.buyStocks(customUserDetails.getUser(), companyId, price, stockCount);
        OrderResponseDto orderResponseDto = stockMapper.orderToDto(order);
        return new ResponseEntity<>(orderResponseDto, HttpStatus.OK);
    }

    //매도
    @PostMapping("/sell")
    public ResponseEntity sellStocks(@RequestParam(name = "companyId") long companyId,
                                     @RequestParam(name = "price") long price,
                                     @RequestParam(name = "stockCount") int stockCount,
                                     @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Order order = orderService.sellStocks(customUserDetails.getUser(), companyId, price, stockCount);
        OrderResponseDto orderResponseDto = stockMapper.orderToDto(order);
        return new ResponseEntity<>(orderResponseDto, HttpStatus.OK);
    }

    //user의 보유 주식 정보를 반환함
    @GetMapping("/holdingStocks")
    public ResponseEntity getHoldingStocks(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<HoldingStockResponseDto> holdingStockResponseDtos = holdingStockService.findHoldingStocks(customUserDetails.getUser().getId());
        holdingStockResponseDtos = holdingStockService.setPercentage(holdingStockResponseDtos);
        return new ResponseEntity<>(holdingStockResponseDtos, HttpStatus.OK);
    }

    //미 체결된 매수, 매도 삭제함
    @DeleteMapping("/orders")
    public void deleteOrders(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                             @RequestParam("stockOrderId") long orderId,
                             @RequestParam("stockCount") int stockCount) {
        orderService.deleteOrder(customUserDetails.getUser(), orderId, stockCount);
    }

    //스케줄러에서 자동으로 30분마다 checkOrder를 해주고 있음 (이 api는 테스트용임)
    @GetMapping("checkOrder")
    public ResponseEntity checkOrder() {
        orderService.checkOrder();

        return new ResponseEntity(HttpStatus.OK);
    }
}
