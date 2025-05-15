package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.kis.service.ApiCallService;
import com.example.jammoney.cash.repository.CashRepository;
import com.example.jammoney.stockApp.stock.repository.CompanyRepository;
import com.example.jammoney.stockApp.stock.repository.OrderRepository;
import com.example.jammoney.stockApp.stock.dto.OrderRequestDto;
import com.example.jammoney.stockApp.stock.dto.OrderResponseDto;
import com.example.jammoney.stockApp.stock.entity.*;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderStatus;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderType;
import com.example.jammoney.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ApiCallService apiCallService;
    private final CompanyRepository companyRepository;
    private final OrderRepository orderRepository;
    private final HoldService holdService;
    private final CashRepository cashRepository;

    @Transactional
    public OrderResponseDto placeBuyOrder(OrderRequestDto dto, User user) {
        Company company = companyRepository.findByCode(dto.getCompanyCode())
                .orElseThrow(() -> new RuntimeException("유효하지 않은 종목 코드입니다."));

        long price = resolvePrice(dto);
        long totalAmount = price * dto.getStockCount();

        holdService.decreaseCash(user, totalAmount);
        holdService.increaseHolding(user, company, dto.getStockCount(), totalAmount);

        Order order = new Order();
        order.setUser(user);
        order.setCompany(company);
        order.setOrderType(OrderType.BUY);
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setPrice(price);
        order.setStockCount(dto.getStockCount());
        orderRepository.save(order);

        return toDto(order, user);
    }

    @Transactional
    public OrderResponseDto placeSellOrder(OrderRequestDto dto, User user) {
        Company company = companyRepository.findByCode(dto.getCompanyCode())
                .orElseThrow(() -> new RuntimeException("유효하지 않은 종목 코드입니다."));

        long price = resolvePrice(dto);
        long totalAmount = price * dto.getStockCount();

        holdService.decreaseHolding(user, company, dto.getStockCount());
        holdService.increaseCash(user, totalAmount);

        Order order = new Order();
        order.setUser(user);
        order.setCompany(company);
        order.setOrderType(OrderType.SELL);
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setPrice(price);
        order.setStockCount(dto.getStockCount());
        orderRepository.save(order);

        return toDto(order, user);
    }

    private long resolvePrice(OrderRequestDto dto) {
        if (dto.getTradeType().equalsIgnoreCase("MARKET")) {
            Object raw = apiCallService.getCurrentPrice(dto.getCompanyCode());
            Map<String, String> output = (Map<String, String>) ((Map<?, ?>) raw).get("output");
            return Long.parseLong(output.get("stck_prpr"));
        } else {
            return dto.getPrice();
        }
    }

    private OrderResponseDto toDto(Order order, User user) {
        return OrderResponseDto.builder()
                .stockOrderId(order.getStockOrderId())
                .userId(user.getId())
                .companyId(order.getCompany().getCompanyId())
                .stockCount(order.getStockCount())
                .price(order.getPrice())
                .orderType(order.getOrderType())
                .orderStatus(order.getOrderStatus())
                .build();
    }
}
