package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.cash.service.CashService;
import com.example.jammoney.exception.ErrorCode;
import com.example.jammoney.exception.StockLogicException;
import com.example.jammoney.stockApp.stock.dto.OrderResponseDto;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderStatus;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderType;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.stockApp.stock.entity.Order;
import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import com.example.jammoney.stockApp.stock.event.OrderChangedEvent;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.repository.HoldingStockRepository;
import com.example.jammoney.stockApp.stock.repository.OrderRepository;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final StockAskingPriceService stockAskingPriceService;
    private final UserPortfolioService userPortfolioService;
    private final CompanyService companyService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final HoldingStockService holdingStockService;
    private final HoldingStockRepository holdingStockRepository;
    private final CashService cashService;
    private final StockMapper stockMapper;
    private final ApplicationEventPublisher eventPublisher;

    private long safeParseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return -1; }
    }

    private int safeParseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }

    @Transactional
    public Order buyStocks(User user, long companyId, long price, int stockCount) {
        cashService.checkCash(price * stockCount, user);
        StockAskingPrice stockAskingPrice = stockAskingPriceService.getStockAskingPrice(companyId);

        for (int i = 1; i <= 10; i++) {
            long askPrice = safeParseLong(stockAskingPrice.getAskp(i));
            int askQty = safeParseInt(stockAskingPrice.getAskp_rsqn(i));

            if (askPrice == price && askQty >= stockCount) {
                return executeBuy(user, price, stockCount, companyId);
            }
        }
        return reserveStock(user, price, stockCount, companyId, OrderType.BUY);
    }

    @Transactional
    public Order sellStocks(User user, long companyId, long price, int stockCount) {
        HoldingStock holdingStock = holdingStockService.findHoldingStock(companyId, user.getId());
        if (holdingStock.getStockCount() < stockCount) throw new StockLogicException(ErrorCode.INSUFFICIENT_STOCK);

        StockAskingPrice stockAskingPrice = stockAskingPriceService.getStockAskingPrice(companyId);

        for (int i = 1; i <= 10; i++) {
            long bidPrice = safeParseLong(stockAskingPrice.getBidp(i));
            int bidQty = safeParseInt(stockAskingPrice.getBidp_rsqn(i));

            if (bidPrice == price && bidQty >= stockCount) {
                return executeSell(user, price, stockCount, companyId);
            }
        }
        return reserveStock(user, price, stockCount, companyId, OrderType.SELL);
    }

    private Order executeBuy(User user, long price, int stockCount, long companyId) {
        HoldingStock holdingStock = holdingStockService.getOrCreateHoldingStock(companyId, user.getId());
        holdingStock.setStockCount(holdingStock.getStockCount() + stockCount);
        holdingStock.setTotalPrice(holdingStock.getTotalPrice() + (price * stockCount));

        Order order = buildOrder(user, companyId, price, stockCount, OrderType.BUY, OrderStatus.COMPLETED);
        updateUserCash(user, -price * stockCount);

        orderRepository.save(order);
        userRepository.save(user);
        holdingStockRepository.save(holdingStock);


        userPortfolioService.updateUserPortfolio(user);

        publishOrderEvent(user.getId());

        return order;
    }

    private Order executeSell(User user, long price, int stockCount, long companyId) {
        HoldingStock holdingStock = holdingStockService.findHoldingStock(companyId, user.getId());
        BigDecimal avgPrice = BigDecimal.valueOf(holdingStock.getTotalPrice())
                .divide(BigDecimal.valueOf(holdingStock.getStockCount() + holdingStock.getReserveStockCount()), RoundingMode.HALF_UP);
        holdingStock.setTotalPrice(holdingStock.getTotalPrice() - avgPrice.multiply(BigDecimal.valueOf(stockCount)).longValue());
        holdingStock.setStockCount(holdingStock.getStockCount() - stockCount);

        Order order = buildOrder(user, companyId, price, stockCount, OrderType.SELL, OrderStatus.COMPLETED);
        updateUserCash(user, price * stockCount);

        orderRepository.save(order);
        userRepository.save(user);
        if (holdingStock.getStockCount() + holdingStock.getReserveStockCount() == 0) {
            holdingStockRepository.delete(holdingStock);
        } else {
            holdingStockRepository.save(holdingStock);
        }


        userPortfolioService.updateUserPortfolio(user);

        publishOrderEvent(user.getId());

        return order;
    }

    private Order buildOrder(User user, long companyId, long price, int stockCount, OrderType type, OrderStatus status) {
        Order order = new Order();
        order.setOrderType(type);
        order.setOrderStatus(status);
        order.setCompany(companyService.findCompanyById(companyId));
        order.setPrice(price);
        order.setStockCount(stockCount);
        order.setUser(user);
        return order;
    }

    private void updateUserCash(User user, long amount) {
        Cash cash = user.getCash();
        cash.setMoney(cash.getMoney() + amount);
        user.setCash(cash);
    }

    @Transactional
    public Order reserveStock(User user, long price, int stockCount, long companyId, OrderType type) {
        if (type == OrderType.SELL) {
            HoldingStock holdingStock = holdingStockService.findHoldingStock(companyId, user.getId());
            holdingStock.setStockCount(holdingStock.getStockCount() - stockCount);
            holdingStock.setReserveStockCount(holdingStock.getReserveStockCount() + stockCount);
            holdingStockRepository.save(holdingStock);
        }
        Order order = buildOrder(user, companyId, price, stockCount, type, OrderStatus.WAITING);
        orderRepository.save(order);
        return order;
    }

    @Transactional
    public void deleteOrder(User user, long orderId, int stockCount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new StockLogicException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getUser().getId().equals(user.getId()))
            throw new StockLogicException(ErrorCode.ORDER_PERMISSION_DENIED);
        if (order.getOrderStatus() != OrderStatus.WAITING)
            throw new StockLogicException(ErrorCode.ORDER_ALREADY_FINISH);

        if (order.getStockCount() <= stockCount) {
            orderRepository.delete(order);
        } else {
            order.setStockCount(order.getStockCount() - stockCount);
            orderRepository.save(order);
        }

        if (order.getOrderType() == OrderType.SELL) {
            HoldingStock holdingStock = holdingStockService.findHoldingStock(order.getCompany().getCompanyId(), user.getId());
            holdingStock.setStockCount(holdingStock.getStockCount() + stockCount);
            holdingStock.setReserveStockCount(holdingStock.getReserveStockCount() - stockCount);
            holdingStockRepository.save(holdingStock);
        }
    }

    @Transactional
    public void checkOrder() {
        List<Company> companyList = companyService.findAllCompanies();
        List<Order> updateBuyOrders = new ArrayList<>();
        List<Order> updateSellOrders = new ArrayList<>();

        for (Company company : companyList) {
            StockAskingPrice stockAskingPrice = stockAskingPriceService.getStockAskingPrice(company.getCompanyId());
            Queue<Order> orderQueue = new LinkedList<>(orderRepository.findAllByCompany_CompanyIdAndOrderStatus(company.getCompanyId(), OrderStatus.WAITING));

            while (!orderQueue.isEmpty()) {
                Order order = orderQueue.poll();
                if (order.getOrderType() == OrderType.BUY) {
                    if (matchBuy(stockAskingPrice, order)) updateBuyOrders.add(order);
                } else {
                    if (matchSell(stockAskingPrice, order)) updateSellOrders.add(order);
                }
            }
        }

        publishGroupedEvents(updateBuyOrders, true);
        publishGroupedEvents(updateSellOrders, false);
    }

    private boolean matchBuy(StockAskingPrice stockAskingPrice, Order order) {
        for (int i = 1; i <= 10; i++) {
            long askPrice = safeParseLong(stockAskingPrice.getAskp(i));
            int askQty = safeParseInt(stockAskingPrice.getAskp_rsqn(i));
            if (askPrice == order.getPrice() && askQty >= order.getStockCount()) {
                executeBuy(order.getUser(), order.getPrice(), order.getStockCount(), order.getCompany().getCompanyId());
                return true;
            }
        }
        return false;
    }

    private boolean matchSell(StockAskingPrice stockAskingPrice, Order order) {
        for (int i = 1; i <= 10; i++) {
            long bidPrice = safeParseLong(stockAskingPrice.getBidp(i));
            int bidQty = safeParseInt(stockAskingPrice.getBidp_rsqn(i));
            if (bidPrice == order.getPrice() && bidQty >= order.getStockCount()) {
                executeSell(order.getUser(), order.getPrice(), order.getStockCount(), order.getCompany().getCompanyId());
                return true;
            }
        }
        return false;
    }

    private void publishOrderEvent(Long userId) {
        List<OrderResponseDto> buyOrders = stockMapper.ordersToDto(orderRepository.findAllByUser_IdAndOrderType(userId, OrderType.BUY));
        List<OrderResponseDto> sellOrders = stockMapper.ordersToDto(orderRepository.findAllByUser_IdAndOrderType(userId, OrderType.SELL));
        eventPublisher.publishEvent(new OrderChangedEvent(userId, buyOrders, sellOrders));
    }

    private void publishGroupedEvents(List<Order> orders, boolean isBuy) {
        orders.stream()
                .collect(Collectors.groupingBy(order -> order.getUser().getId()))
                .forEach((userId, userOrders) -> {
                    List<OrderResponseDto> dtos = stockMapper.ordersToDto(userOrders);
                    if (isBuy) {
                        eventPublisher.publishEvent(new OrderChangedEvent(userId, dtos, List.of()));
                    } else {
                        eventPublisher.publishEvent(new OrderChangedEvent(userId, List.of(), dtos));
                    }
                });
    }
}

