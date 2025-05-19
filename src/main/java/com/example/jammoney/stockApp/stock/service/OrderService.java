package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.cash.service.CashService;
import com.example.jammoney.exception.StockLogicException;
import com.example.jammoney.exception.ErrorCode;
import com.example.jammoney.stockApp.stock.dto.OrderResponseDto;
import com.example.jammoney.stockApp.stock.entity.*;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderStatus;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderType;
import com.example.jammoney.stockApp.stock.event.OrderChangedEvent;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.repository.HoldingStockRepository;
import com.example.jammoney.stockApp.stock.repository.OrderRepository;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final StockAskingPriceService stockAskingPriceService;
    private final CompanyService companyService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final HoldingStockService holdingStockService;
    private final HoldingStockRepository holdingStockRepository;
    private final CashService cashService;
    private final StockMapper stockMapper;
    private final ApplicationEventPublisher eventPublisher;

    public Order buyStocks(User user, long companyId, long price, int stockCount) {
        //회원 캐쉬 잔량 비교
        cashService.checkCash(price * stockCount, user); // -> 부족할 시 예외 처리
        //호가 불러오기
        StockAskingPrice stockAskingPrice = stockAskingPriceService.getStockAskingPrice(companyId);
        // 예약 구매인지 바로 구매인지 판별
        return buyDiscrimination(user, price, stockAskingPrice, stockCount, companyId);
    }

    private Order buyDiscrimination(User user, long price, StockAskingPrice stockAskingPrice, int stockCount, long companyId) {
        // 매도 호가와 가격이 같고, 잔량이 남아 있을 경우
        if(Long.parseLong(stockAskingPrice.getAskp1()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn1()) > stockCount)
            return buyStock(user, price, stockCount, companyId); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp2()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn2()) > stockCount)
            return buyStock(user, price, stockCount, companyId); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp3()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn3()) > stockCount)
            return buyStock(user, price, stockCount, companyId); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp4()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn4()) > stockCount)
            return buyStock(user, price, stockCount, companyId); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp5()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn5()) > stockCount)
            return buyStock(user, price, stockCount, companyId); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp6()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn6()) > stockCount)
            return buyStock(user, price, stockCount, companyId); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp7()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn7()) > stockCount)
            return buyStock(user, price, stockCount, companyId); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp8()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn8()) > stockCount)
            return buyStock(user, price, stockCount, companyId); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp9()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn9()) > stockCount)
            return buyStock(user, price, stockCount, companyId); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp10()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn10()) > stockCount)
            return buyStock(user, price, stockCount, companyId); // 구매 로직
        else
            return reserveStock(user, price, stockCount, companyId, OrderType.BUY); //예약 구매 로직
    }

    public Order buyStock(User user, long price, int stockCount, long companyId) {
        // 보유 주식 설정
        HoldingStock holdingStock = holdingStockService.checkHoldingStock(companyId, user.getId());
        holdingStock.setStockCount(holdingStock.getStockCount() + stockCount);
        holdingStock.setTotalPrice(holdingStock.getTotalPrice() + (stockCount * price));

        // 주문 생성
        Order order = new Order();
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setOrderType(OrderType.BUY);
        order.setStockCount(stockCount);
        order.setPrice(price);
        order.setCompany(companyService.findCompanyById(companyId));

        // 현금량 감소
        Cash cash = user.getCash();
        cash.setMoney(cash.getMoney()-(price * stockCount));
        user.setCash(cash);
        order.setUser(user);

        orderRepository.save(order);
        userRepository.save(user);
        holdingStockRepository.save(holdingStock);

        //주문 체결되었으므로 롱 폴링 응답 발행
        List<OrderResponseDto> buyOrders = getUpdatedBuyOrders(user.getId());
        eventPublisher.publishEvent(new OrderChangedEvent(user.getId(), buyOrders, List.of()));


        return order;
    }

    // 예약 매도 일 때는 보유 주식 줄어들게(완료)
    public Order reserveStock(User user, long price, int stockCount, long companyId, OrderType type) {
        if(OrderType.SELL.equals(type)) {
            // 보유 주식 설정
            HoldingStock holdingStock = holdingStockService.findHoldingStock(companyId, user.getId());
            holdingStock.setStockCount(holdingStock.getStockCount() - stockCount);
            holdingStock.setReserveStockCount(stockCount);

        }
        Order order = new Order();
        order.setOrderStatus(OrderStatus.WAITING);
        order.setOrderType(type);
        order.setStockCount(stockCount);
        order.setPrice(price);
        order.setCompany(companyService.findCompanyById(companyId));
        order.setUser(user);

        orderRepository.save(order);

        return order;
    }

    public void checkOrder() {
        // 회사 리스트를 받아온다
        List<Company> companyList = companyService.findAllCompanies();
        List<Order> updateBuyStockOrders = new ArrayList<>();
        List<Order> updateSellStockOrders = new ArrayList<>();
        // for문(회사별로)
        for(Company company : companyList) {
            // 회사 호가 리스트를 받아온다
            StockAskingPrice stockAskingPrice = stockAskingPriceService.getStockAskingPrice(company.getCompanyId());
            // 회사Id에 있는 stockOrder 중 체결 대기 상태인 stockOrder를 큐로 받아온다
            Queue<Order> stockOrderQueue = getOrderQueue(company.getCompanyId(), OrderStatus.WAITING);
            //큐가 비어있지 않으면
            if(!stockOrderQueue.isEmpty()) {
                // for문(큐가 다 빌 때 까지 실행한다)
                while(!stockOrderQueue.isEmpty()) {
                    Order order = stockOrderQueue.poll();
                    // 예약 매수 실행
                    if(order.getOrderType().equals(OrderType.BUY)) {
                        // 호가 리스트 안에 체결 대기중인 stockOrder의 조건이 맞는 것이 있으면 buyStock으로 간다
                        Order buyStock = reserveBuyDiscrimination(stockAskingPrice, order);
                        // 클라이언트로 StockOrder를 보낸다(값이 있으면)
                        if(buyStock != null)
                            updateBuyStockOrders.add(buyStock);
                    }
                    // 예약 매도 실행
                    else {
                        Order sellStock = reserveSellDiscrimination(stockAskingPrice, order);
                        // 클라이언트로 StockOrder를 보낸다(값이 있으면)
                        if(sellStock != null)
                            updateSellStockOrders.add(sellStock);
                    }
                }
            }
        }
        // BUY 주문 그룹핑 후 이벤트 발행
        updateBuyStockOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getUser().getId()))
                .forEach((userId, orders) -> {
                    List<OrderResponseDto> buyDtos = stockMapper.ordersToDto(orders);
                    eventPublisher.publishEvent(new OrderChangedEvent(userId, buyDtos, List.of()));
                });

// SELL 주문 그룹핑 후 이벤트 발행
        updateSellStockOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getUser().getId()))
                .forEach((userId, orders) -> {
                    List<OrderResponseDto> sellDtos = stockMapper.ordersToDto(orders);
                    eventPublisher.publishEvent(new OrderChangedEvent(userId, List.of(), sellDtos));
                });
    }



    private Order reserveBuyDiscrimination(StockAskingPrice stockAskingPrice, Order order) {
        long price = order.getPrice();
        int stockCount = order.getStockCount();
        // 매도 호가와 가격이 같고, 잔량이 남아 있을 경우
        if(Long.parseLong(stockAskingPrice.getAskp1()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn1()) > stockCount)
            return reserveBuyStock(order); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp2()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn2()) > stockCount)
            return reserveBuyStock(order); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp3()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn3()) > stockCount)
            return reserveBuyStock(order); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp4()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn4()) > stockCount)
            return reserveBuyStock(order); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp5()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn5()) > stockCount)
            return reserveBuyStock(order); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp6()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn6()) > stockCount)
            return reserveBuyStock(order); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp7()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn7()) > stockCount)
            return reserveBuyStock(order); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp8()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn8()) > stockCount)
            return reserveBuyStock(order); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp9()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn9()) > stockCount)
            return reserveBuyStock(order); // 구매 로직
        else if(Long.parseLong(stockAskingPrice.getAskp10()) == price && Integer.parseInt(stockAskingPrice.getAskp_rsqn10()) > stockCount)
            return reserveBuyStock(order); // 구매 로직
        else {
            return null; // 아무것도 안함
        }

    }

    private Order reserveSellDiscrimination(StockAskingPrice stockAskingPrice, Order order) {
        long price = order.getPrice();
        int stockCount = order.getStockCount();
        // 매도 호가와 가격이 같고, 잔량이 남아 있을 경우
        if(Long.parseLong(stockAskingPrice.getBidp1()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn1()) > stockCount)
            return reserveSellStock(order); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp2()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn2()) > stockCount)
            return reserveSellStock(order); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp3()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn3()) > stockCount)
            return reserveSellStock(order); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp4()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn4()) > stockCount)
            return reserveSellStock(order); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp5()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn5()) > stockCount)
            return reserveSellStock(order); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp6()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn6()) > stockCount)
            return reserveSellStock(order); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp7()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn7()) > stockCount)
            return reserveSellStock(order); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp8()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn8()) > stockCount)
            return reserveSellStock(order); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp9()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn9()) > stockCount)
            return reserveSellStock(order); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp10()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn10()) > stockCount)
            return reserveSellStock(order); // 판매 로직
        else
            return null; // 아무것도 안함

    }

    // 예약된 매수 -> 구매 상태로 바구기
    public Order reserveBuyStock(Order order) {
        Optional<Order> optionalStockOrder = orderRepository.findById(order.getOrderId());
        Order updateStockOrder = optionalStockOrder.get();
        updateStockOrder.setOrderStatus(OrderStatus.COMPLETED);
        updateStockOrder.setOrderType(OrderType.BUY);
        // 보유 주식 설정
        HoldingStock holdingStock = holdingStockService.checkHoldingStock(order.getCompany().getCompanyId(), order.getUser().getId());
        holdingStock.setStockCount(holdingStock.getStockCount() + order.getStockCount());
        holdingStock.setTotalPrice(holdingStock.getTotalPrice() + (order.getStockCount() * order.getPrice()));
        // 현금량 감소
        User user = updateStockOrder.getUser();
        Cash cash = user.getCash();
        cash.setMoney(cash.getMoney()-(order.getPrice() * order.getStockCount()));
        user.setCash(cash);
        order.setUser(user);

        orderRepository.save(order);
        userRepository.save(user);
        holdingStockRepository.save(holdingStock);

        return order;
    }

    // 예약 매도 -> 판매로 바뀔 때 금액 늘어나게, 보유 주식은 예약 할 때 줄어들도록
    // Price 줄어드는 금액은 주식 투자금액 - (주식 투자 금액 / 보유 주식 개수) * 팔 주식 개수 (완료)
    public Order reserveSellStock(Order order) {
        Optional<Order> optionalStockOrder = orderRepository.findById(order.getOrderId());
        Order updateStockOrder = optionalStockOrder.get();
        updateStockOrder.setOrderStatus(OrderStatus.COMPLETED);
        updateStockOrder.setOrderType(OrderType.SELL);
        // 보유 주식 설정
        HoldingStock holdingStock = holdingStockService.findHoldingStock(order.getCompany().getCompanyId(), order.getUser().getId());
        holdingStock.setTotalPrice(holdingStock.getTotalPrice() - (holdingStock.getTotalPrice() / (holdingStock.getStockCount()+holdingStock.getReserveStockCount())) * order.getStockCount());
        holdingStock.setReserveStockCount(holdingStock.getReserveStockCount() - order.getStockCount());
        // 현금량 증가
        User user = updateStockOrder.getUser();
        Cash cash = user.getCash();
        cash.setMoney(cash.getMoney() + (order.getPrice() * order.getStockCount()));
        user.setCash(cash);
        order.setUser(user);

        orderRepository.save(order);
        userRepository.save(user);
        if(holdingStock.getStockCount() + holdingStock.getReserveStockCount() == 0)
            holdingStockRepository.delete(holdingStock);
        else
            holdingStockRepository.save(holdingStock);

        return order;
    }

    // 멤버, 회사 id, 가격
    public Order sellStocks(User user   , long companyId, long price, int stockCount) {
        // 내가 주식을 가지고 있는지 없는지 판별
        HoldingStock holdingStock = holdingStockService.findHoldingStock(companyId, user.getId());
        if(holdingStock.getStockCount() < stockCount)
            throw new StockLogicException(ErrorCode.INSUFFICIENT_STOCK);
        else {
            //호가 불러오기
            StockAskingPrice stockAskingPrice = stockAskingPriceService.getStockAskingPrice(companyId);
            // 예약 판매인지 바로 판매인지 판별
            return sellDiscrimination(user, price, stockAskingPrice, stockCount, companyId);
        }

    }

    // 매도 판별해서 실행
    public Order sellDiscrimination(User user, long price, StockAskingPrice stockAskingPrice, int stockCount, long companyId) {
        // 매도 호가와 가격이 같고, 잔량이 남아 있을 경우
        if(Long.parseLong(stockAskingPrice.getBidp1()) == price && Integer.parseInt(stockAskingPrice.getBidp1()) > stockCount)
            return sellStock(user, price, stockCount, companyId); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp2()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn2()) > stockCount)
            return sellStock(user, price, stockCount, companyId); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp3()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn3()) > stockCount)
            return sellStock(user, price, stockCount, companyId); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp4()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn4()) > stockCount)
            return sellStock(user, price, stockCount, companyId); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp5()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn5()) > stockCount)
            return sellStock(user, price, stockCount, companyId); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp6()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn6()) > stockCount)
            return sellStock(user, price, stockCount, companyId); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp7()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn7()) > stockCount)
            return sellStock(user, price, stockCount, companyId); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp8()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn8()) > stockCount)
            return sellStock(user, price, stockCount, companyId); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp9()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn9()) > stockCount)
            return sellStock(user, price, stockCount, companyId); // 판매 로직
        else if(Long.parseLong(stockAskingPrice.getBidp10()) == price && Integer.parseInt(stockAskingPrice.getBidp_rsqn10()) > stockCount)
            return sellStock(user, price, stockCount, companyId); // 판매 로직
        else
            return reserveStock(user, price, stockCount, companyId, OrderType.SELL); //예약 판매 로직
    }

    public Order sellStock(User user, long price, int stockCount, long companyId) {
        // 보유 주식 설정
        HoldingStock holdingStock = holdingStockService.findHoldingStock(companyId, user.getId());
        holdingStock.setTotalPrice(holdingStock.getTotalPrice() - (holdingStock.getTotalPrice() / (holdingStock.getStockCount()+holdingStock.getReserveStockCount())) * stockCount);
        holdingStock.setStockCount(holdingStock.getStockCount() - stockCount);

        // 스톡 오더 작성
        Order stockOrder = new Order();
        stockOrder.setOrderStatus(OrderStatus.COMPLETED);
        stockOrder.setOrderType(OrderType.SELL);
        stockOrder.setStockCount(stockCount);
        stockOrder.setPrice(price);
        stockOrder.setCompany(companyService.findCompanyById(companyId));

        // 현금량 증가
        Cash cash = user.getCash();
        cash.setMoney(cash.getMoney()+(price * stockCount));
        user.setCash(cash);
        stockOrder.setUser(user);



        orderRepository.save(stockOrder);
        userRepository.save(user);

        // 여기서 holdingStock 삭제됨
        // 예약 매도 걸 때 reserveStockCount 늘어가네
        // 예약 매도 취소 할 때 reserveStockCount 줄어들게
        if(holdingStock.getStockCount() + holdingStock.getReserveStockCount() == 0)
            holdingStockRepository.delete(holdingStock);
        else
            holdingStockRepository.save(holdingStock);

        //주문 체결되었으므로 롱 폴링 응답 발행
        List<OrderResponseDto> sellOrders = getUpdatedSellOrders(user.getId());
        eventPublisher.publishEvent(new OrderChangedEvent(user.getId(), List.of(), sellOrders));

        return stockOrder;
    }


    // 거래 대기중인 매수 Order 불러오기
    public Queue<Order> getOrderQueue(long companyId, OrderStatus orderStates) {
        List<Order> stockOrderList = orderRepository.findAllByCompany_CompanyIdAndOrderStatus(companyId, orderStates);
        return new LinkedList<>(stockOrderList);
    }

    // 멤버의 모든 주식 거래내역 삭제하기
    public void deleteAllOrders(User user) {
        List<Order> stockOrders = orderRepository.findAllByUser_Id(user.getId());

        orderRepository.deleteAll(stockOrders);
    }

    // 멤버의 모든 order 불러오기
    public List<OrderResponseDto> getUserStockOrders(long userId) {
        List<Order> stockOrders = orderRepository.findAllByUser_IdOrderByModifiedAtDesc(userId);

        return stockOrders.stream()
                .map(stockMapper::orderToDto).collect(Collectors.toList());
    }

    // 예약된 order 취소하는 메소드
    // 취소한 주식 수 만큼 보유주식으로 돌아오게 해야함
    public void deleteOrder(User user, long stockOrderId, int stockCount) {
        Optional<Order> optionalStockOrder = orderRepository.findById(stockOrderId);
        Order order = optionalStockOrder.orElseThrow(() -> new StockLogicException(ErrorCode.ORDER_NOT_FOUND));

        if(order.getUser().getId() != user.getId()) {
            throw new StockLogicException(ErrorCode.ORDER_PERMISSION_DENIED);
        }
        else if(!order.getOrderStatus().equals(OrderStatus.WAITING))
            throw new StockLogicException(ErrorCode.ORDER_ALREADY_FINISH);
            // 수량 선택해서 취소 할 수 있게(취소한 만큼 보유 주식 돌아오게) 0이 되면 미체결 스톡 오더 삭제
        else {
            //예약된 수량 전부 다 취소면 order 삭제
            if(order.getStockCount() <= stockCount)
                orderRepository.delete(order);
            //취소하는 주식 수 만큼 stockCount에서 차감
            else {
                order.setStockCount(order.getStockCount() - stockCount);
            }

            //만약 매도 주문을 취소하는 거라면 보유 주식에 대해서 보유 주식 수와 예약된 보유 주식 수 갱신
            if(OrderType.SELL.equals(order.getOrderType())) {
                HoldingStock holdingStock = holdingStockService.findHoldingStock(order.getCompany().getCompanyId(), order.getUser().getId());
                holdingStock.setStockCount(holdingStock.getStockCount() + stockCount);
                holdingStock.setReserveStockCount(holdingStock.getReserveStockCount() - stockCount);
            }

        }
    }

//    public void checkOrderAndNotify(Order order) {
//        // 주문 상태 업데이트 로직 수행 (예: WAITING → SUCCESS)
//        updateOrderStatusIfMatched(order);
//        Long userId = order.getUser().getId();
//        List<OrderResponseDto> buyOrders = getUpdatedBuyOrders(userId); // 직접 구현 필요
//        List<OrderResponseDto> sellOrders = getUpdatedSellOrders(userId); // 직접 구현 필요
//
//        // 이벤트 발행
//        eventPublisher.publishEvent(new OrderChangedEvent(userId, buyOrders, sellOrders));
//    }

    private List<OrderResponseDto> getUpdatedSellOrders(Long userId) {
        List<Order> sellOrders = orderRepository.findAllByUser_IdAndOrderType(userId, OrderType.SELL);
        return stockMapper.ordersToDto(sellOrders);
    }

    private List<OrderResponseDto> getUpdatedBuyOrders(Long userId) {
        List<Order> buyOrders = orderRepository.findAllByUser_IdAndOrderType(userId, OrderType.BUY);
        return stockMapper.ordersToDto(buyOrders);
    }
}
