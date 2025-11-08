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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final StockAskingPriceService stockAskingPriceService;
    private final UserPortfolioService userPortfolioService;
    private final CompanyService companyService;
    private final OrderRepository orderRepository;
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
        long amount = Math.multiplyExact(price, (long) stockCount);

        // 사용 가능 현금 검증
        cashService.assertAvailable(amount, user);

        StockAskingPrice asking = stockAskingPriceService.getStockAskingPrice(companyId);

        // 즉시 체결 가능?
        if (canBuyImmediately(asking, price, stockCount)) {
            Order order = buildOrder(user, companyId, price, stockCount, OrderType.BUY, OrderStatus.COMPLETED);
            settleImmediateBuy(user, companyId, price, stockCount);
            // 신규 생성은 save 필요
            order.setExecutedPrice(price);
            order.setExecutedAt(Instant.now());
            orderRepository.save(order);

            userPortfolioService.updateUserPortfolio(user);
            publishOrderEvent(user.getId());
            return order;
        }

        // 예약 주문: 현금 "예약"으로 잠금
        cashService.reserve(amount, user);
        Order order = buildOrder(user, companyId, price, stockCount, OrderType.BUY, OrderStatus.WAITING);
        orderRepository.save(order);

        publishOrderEvent(user.getId());
        return order;
    }

    @Transactional
    public Order sellStocks(User user, long companyId, long price, int stockCount) {
        HoldingStock holding = holdingStockService.findHoldingStock(companyId, user.getId());
        if (holding.getStockCount() < stockCount) throw new StockLogicException(ErrorCode.INSUFFICIENT_STOCK);

        StockAskingPrice asking = stockAskingPriceService.getStockAskingPrice(companyId);

        // 즉시 체결 가능?
        if (canSellImmediately(asking, price, stockCount)) {
            Order order = buildOrder(user, companyId, price, stockCount, OrderType.SELL, OrderStatus.COMPLETED);
            settleImmediateSell(user, companyId, price, stockCount);
            order.setExecutedPrice(price);
            order.setExecutedAt(Instant.now());
            orderRepository.save(order);

            userPortfolioService.updateUserPortfolio(user);
            publishOrderEvent(user.getId());
            return order;
        }

        // 예약 주문: 주식 "예약수량"으로 이동(가용수량 ↓, 예약수량 ↑)
        holding.setStockCount(holding.getStockCount() - stockCount);
        holding.setReserveStockCount(holding.getReserveStockCount() + stockCount);
        // 기존 엔티티는 flush/dc로 반영되므로 save 불필요

        Order order = buildOrder(user, companyId, price, stockCount, OrderType.SELL, OrderStatus.WAITING);
        orderRepository.save(order);

        publishOrderEvent(user.getId());
        return order;
    }

    @Transactional
    public void deleteOrder(User user, long orderId, int cancelCount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new StockLogicException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId()))
            throw new StockLogicException(ErrorCode.ORDER_PERMISSION_DENIED);
        if (order.getOrderStatus() != OrderStatus.WAITING)
            throw new StockLogicException(ErrorCode.ORDER_ALREADY_FINISH);
        if (cancelCount <= 0)
            throw new StockLogicException(ErrorCode.VALIDATION_ERROR);

        int remain = order.getStockCount() - cancelCount;
        if (remain < 0) throw new StockLogicException(ErrorCode.VALIDATION_ERROR);

        long amountEach = Math.multiplyExact(order.getPrice(), 1L);
        long cancelAmount = Math.multiplyExact(order.getPrice(), (long) cancelCount);

        if (order.getOrderType() == OrderType.BUY) {
            // 예약 현금 환원
            cashService.releaseReserved(cancelAmount, user);
        } else {
            // 예약 주식 환원 (reserve → stock)
            HoldingStock holding = holdingStockService.findHoldingStock(order.getCompany().getCompanyId(), user.getId());
            holding.setReserveStockCount(holding.getReserveStockCount() - cancelCount);
            holding.setStockCount(holding.getStockCount() + cancelCount);
        }

        if (remain == 0) {
            orderRepository.delete(order);
        } else {
            order.setStockCount(remain);
            // 대기 수량만 감소
        }

        publishOrderEvent(user.getId());
    }

    /**
     * 주기적 매칭
     */
    @Transactional
    public void checkOrder() {
        List<Company> companies = companyService.findAllCompanies();
        List<Order> toNotifyBuy = new ArrayList<>();
        List<Order> toNotifySell = new ArrayList<>();

        for (Company c : companies) {
            StockAskingPrice asking = stockAskingPriceService.getStockAskingPrice(c.getCompanyId());
            Queue<Order> queue = new LinkedList<>(
                    orderRepository.findAllByCompany_CompanyIdAndOrderStatus(c.getCompanyId(), OrderStatus.WAITING)
            );

            while (!queue.isEmpty()) {
                Order o = queue.poll();
                boolean matched;

                if (o.getOrderType() == OrderType.BUY) {
                    matched = tryFulfillWaitingBuy(o, asking);
                    if (matched) toNotifyBuy.add(o);
                } else {
                    matched = tryFulfillWaitingSell(o, asking);
                    if (matched) toNotifySell.add(o);
                }
            }
        }

        publishGroupedEvents(toNotifyBuy, true);
        publishGroupedEvents(toNotifySell, false);
    }

    public List<Order> getWaitingOrders(Long userId) {
        return orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.WAITING);
    }

    private boolean canBuyImmediately(StockAskingPrice ap, long price, int qty) {
        for (int i = 1; i <= 10; i++) {
            long askPrice = safeParseLong(ap.getAskp(i));
            int askQty = safeParseInt(ap.getAskp_rsqn(i));
            if (askPrice == price && askQty >= qty) return true;
        }
        return false;
    }

    private boolean canSellImmediately(StockAskingPrice ap, long price, int qty) {
        for (int i = 1; i <= 10; i++) {
            long bidPrice = safeParseLong(ap.getBidp(i));
            int bidQty = safeParseInt(ap.getBidp_rsqn(i));
            if (bidPrice == price && bidQty >= qty) return true;
        }
        return false;
    }

    // WAITING(BUY) → COMPLETED 체결 시도
    private boolean tryFulfillWaitingBuy(Order order, StockAskingPrice ap) {
        if (!canBuyImmediately(ap, order.getPrice(), order.getStockCount())) return false;

        long amount = Math.multiplyExact(order.getPrice(), (long) order.getStockCount());

        // 예약금 → 실차감으로 전환(부족 시 실패 처리)
        cashService.commitReservedForBuy(amount, order.getUser());

        // 보유 주식 반영
        HoldingStock holding = holdingStockService.getOrCreateHoldingStock(order.getCompany().getCompanyId(), order.getUser().getId());
        holding.setStockCount(holding.getStockCount() + order.getStockCount());
        holding.setTotalPrice(holding.getTotalPrice() + amount);

        // 주문 상태 전이
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setExecutedPrice(order.getPrice());
        order.setExecutedAt(Instant.now());

        userPortfolioService.updateUserPortfolio(order.getUser());
        publishOrderEvent(order.getUser().getId());
        return true;
    }

    // WAITING(SELL) → COMPLETED 체결 시도
    private boolean tryFulfillWaitingSell(Order order, StockAskingPrice ap) {
        if (!canSellImmediately(ap, order.getPrice(), order.getStockCount())) return false;

        HoldingStock holding = holdingStockService.findHoldingStock(order.getCompany().getCompanyId(), order.getUser().getId());

        // 평균 원가 = totalPrice / (stockCount + reserveStockCount)
        long totalUnits = holding.getStockCount() + holding.getReserveStockCount();
        if (totalUnits <= 0) throw new StockLogicException(ErrorCode.INSUFFICIENT_STOCK);

        BigDecimal avg = BigDecimal.valueOf(holding.getTotalPrice())
                .divide(BigDecimal.valueOf(totalUnits), 8, RoundingMode.HALF_UP);

        int qty = order.getStockCount();
        long costToReduce = avg.multiply(BigDecimal.valueOf(qty)).setScale(0, RoundingMode.HALF_UP).longValue();

        // 예약 체결: reserveStockCount ↓, stockCount는 건드리지 않음(이중 차감 방지)
        holding.setReserveStockCount(holding.getReserveStockCount() - qty);
        holding.setTotalPrice(holding.getTotalPrice() - costToReduce);

        // 현금 유입
        long proceed = Math.multiplyExact(order.getPrice(), (long) qty);
        addMoney(order.getUser(), proceed);

        // 보유가 0이 되면 제거
        if (holding.getStockCount() + holding.getReserveStockCount() == 0) {
            holdingStockRepository.delete(holding);
        }

        // 주문 상태 전이
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setExecutedPrice(order.getPrice());
        order.setExecutedAt(Instant.now());

        userPortfolioService.updateUserPortfolio(order.getUser());
        publishOrderEvent(order.getUser().getId());
        return true;
    }

    private void settleImmediateBuy(User user, long companyId, long price, int qty) {
        long amount = Math.multiplyExact(price, (long) qty);
        HoldingStock holding = holdingStockService.getOrCreateHoldingStock(companyId, user.getId());
        holding.setStockCount(holding.getStockCount() + qty);
        holding.setTotalPrice(holding.getTotalPrice() + amount);
        subMoney(user, amount);
    }

    private void settleImmediateSell(User user, long companyId, long price, int qty) {
        HoldingStock holding = holdingStockService.findHoldingStock(companyId, user.getId());

        long totalUnits = holding.getStockCount() + holding.getReserveStockCount();
        if (totalUnits <= 0) throw new StockLogicException(ErrorCode.INSUFFICIENT_STOCK);

        BigDecimal avg = BigDecimal.valueOf(holding.getTotalPrice())
                .divide(BigDecimal.valueOf(totalUnits), 8, RoundingMode.HALF_UP);

        long costToReduce = avg.multiply(BigDecimal.valueOf(qty)).setScale(0, RoundingMode.HALF_UP).longValue();

        holding.setTotalPrice(holding.getTotalPrice() - costToReduce);
        holding.setStockCount(holding.getStockCount() - qty);

        long proceed = Math.multiplyExact(price, (long) qty);
        addMoney(user, proceed);

        if (holding.getStockCount() + holding.getReserveStockCount() == 0) {
            holdingStockRepository.delete(holding);
        }
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

    private void subMoney(User user, long amount) {
        Cash c = user.getCash();
        c.setMoney(c.getMoney() - amount);
    }

    private void addMoney(User user, long amount) {
        Cash c = user.getCash();
        c.setMoney(c.getMoney() + amount);
    }

    /** 이벤트 처리 */
    private void publishOrderEvent(Long userId) {
        List<OrderResponseDto> buyOrders = stockMapper.ordersToDto(
                orderRepository.findAllByUser_IdAndOrderType(userId, OrderType.BUY)
        );
        List<OrderResponseDto> sellOrders = stockMapper.ordersToDto(
                orderRepository.findAllByUser_IdAndOrderType(userId, OrderType.SELL)
        );
        eventPublisher.publishEvent(new OrderChangedEvent(userId, buyOrders, sellOrders));
    }

    private void publishGroupedEvents(List<Order> orders, boolean isBuy) {
        orders.stream()
                .collect(Collectors.groupingBy(o -> o.getUser().getId()))
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
