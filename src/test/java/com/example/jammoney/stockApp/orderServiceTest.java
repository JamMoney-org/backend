package com.example.jammoney.stockApp;

import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.stockApp.stock.entity.*;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderStatus;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderType;
import com.example.jammoney.stockApp.stock.event.OrderChangedEvent;
import com.example.jammoney.stockApp.stock.repository.CompanyRepository;
import com.example.jammoney.stockApp.stock.repository.HoldingStockRepository;
import com.example.jammoney.stockApp.stock.repository.OrderRepository;
import com.example.jammoney.stockApp.stock.repository.StockAskingPriceRepository;
import com.example.jammoney.stockApp.stock.service.CompanyService;
import com.example.jammoney.stockApp.stock.service.HoldingStockService;
import com.example.jammoney.stockApp.stock.service.OrderService;
import com.example.jammoney.stockApp.stock.service.StockAskingPriceService;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class orderServiceTest {
    @PersistenceContext
    EntityManager em;
    @Autowired TestEventListener testEventListener;
    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    CompanyService companyService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    HoldingStockRepository holdingStockRepository;

    @Autowired
    HoldingStockService holdingStockService;
    @Autowired
    StockAskingPriceService stockAskingPriceService;

    @Autowired
    CompanyRepository companyRepository;

    private User testUser;
    private Company testCompany;
    @Autowired
    private StockAskingPriceRepository stockAskingPriceRepository;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        // 1. 사용자 생성 + 현금 세팅
        testUser = new User();
        testUser.setEmail("test@test.com");
        testUser.setPassword("passwordf#");
        Cash cash = new Cash();
        cash.setMoney(1_000_000L);
        testUser.setCash(cash);
        testUser.setNickname("tester");
        cash.setUser(testUser);
        userRepository.save(testUser);

        // 2. 회사 생성
        Company company = new Company();
        company.setCode("005930");
        company.setKorName("삼성전자");

        // 3. StockInfo 생성 및 연결
        StockInfo info = new StockInfo();
        info.setAcml_vol("100");  // 액면가
        info.setStck_prpr("1_000_000L");  // 상장 주식 수
        info.setPrdy_ctrt("100_000_000L");  // 자본금
        info.setCompany(company);  // 양방향 연결
        company.setStockInfo(info);

        StockAskingPrice ask = new StockAskingPrice();
        for (int i = 1; i <= 10; i++) {
            Field askpField = StockAskingPrice.class.getDeclaredField("askp" + i);
            askpField.setAccessible(true);
            askpField.set(ask, "80000");

            Field askpRsqnField = StockAskingPrice.class.getDeclaredField("askp_rsqn" + i);
            askpRsqnField.setAccessible(true);
            askpRsqnField.set(ask, "100");

            Field bidpField = StockAskingPrice.class.getDeclaredField("bidp" + i);
            bidpField.setAccessible(true);
            bidpField.set(ask, "79000");

            Field bidpRsqnField = StockAskingPrice.class.getDeclaredField("bidp_rsqn" + i);
            bidpRsqnField.setAccessible(true);
            bidpRsqnField.set(ask, "200");
        }
        ask.setCompany(company);

        stockAskingPriceRepository.save(ask);
        company.setStockAskingPrice(ask);
        testCompany = companyRepository.save(company);
        em.flush(); // INSERT 쿼리 강제 실행
        em.clear();
    }

    @Test
    void 예약_매수_주문은_WAITING상태로_저장된다() {
        // given
        long price = 999_999L; // 호가와 맞지 않게 설정
        int count = 1;

        // when
        Order order = orderService.buyStocks(testUser, testCompany.getCompanyId(), price, count);

        // then
        assertEquals(OrderStatus.WAITING, order.getOrderStatus());
        assertEquals(OrderType.BUY, order.getOrderType());
        assertEquals(price, order.getPrice());
    }

    @Test
    void 호가와_일치하면_즉시_매수_체결된다() {
        // given
        long askp1 = Long.parseLong(
                stockAskingPriceService.getStockAskingPrice(testCompany.getCompanyId()).getAskp1()
        );
        int count = 1;

        // when
        Order order = orderService.buyStocks(testUser, testCompany.getCompanyId(), askp1, count);

        // then
        assertEquals(OrderStatus.COMPLETED, order.getOrderStatus());
        assertEquals(OrderType.BUY, order.getOrderType());

        HoldingStock holding = holdingStockRepository.findByCompanyAndUser(
                testCompany.getCompanyId(), testUser.getId()
        );
        assertNotNull(holding);
        assertEquals(count, holding.getStockCount());
    }
    @Test
    void 호가보다_높은_가격으로_매도하면_예약으로_저장된다() {
        // given: 보유 주식 먼저 넣어줘야 함
        holdingStockService.getOrCreateHoldingStock(testCompany.getCompanyId(), testUser.getId())
                .setStockCount(10);

        long price = 999_999L;
        int count = 1;

        // when
        Order order = orderService.sellStocks(testUser, testCompany.getCompanyId(), price, count);

        // then
        assertEquals(OrderStatus.WAITING, order.getOrderStatus());
        assertEquals(OrderType.SELL, order.getOrderType());
    }
    @Test
    void checkOrder가_예약매수를_체결처리한다() {
        // given
        long price = 999_999L;
        int count = 1;

        // 유저에게 주문 전 현금 상태 저장
        long cashBefore = testUser.getCash().getMoney();
        System.out.println(testUser.getCash().getMoney()+"!@@");
        // 1. testCompany의 호가를 먼저 세팅
        StockAskingPrice ask = testCompany.getStockAskingPrice(); // 기존 객체
        ask.setAskp1("888888");
        ask.setAskp_rsqn1("10");
        stockAskingPriceRepository.save(ask);
        testCompany.setStockAskingPrice(ask);
        companyRepository.save(testCompany);  // 영속 연결

        // 2. 예약 매수 (호가와 일부러 다르게 설정)
        testUser = userRepository.findById(testUser.getId()).orElseThrow();

        Order order = orderService.buyStocks(testUser, testCompany.getCompanyId(), price, count);
        assertEquals(OrderStatus.WAITING, order.getOrderStatus());

        // when
        ask.setAskp1("999999"); // 이제 체결되게
        stockAskingPriceRepository.save(ask);
        em.flush();
        em.clear();
        System.out.println("체결 직전 호가: " + stockAskingPriceRepository.findByCompany_CompanyId(testCompany.getCompanyId()).getAskp1());

        orderService.checkOrder(); // checkOrder는 최신 호가 기준으로 체결 판단

        // then
        Order updated = orderRepository.findById(order.getOrderId()).orElseThrow();
        assertEquals(OrderStatus.COMPLETED, updated.getOrderStatus());

        HoldingStock holding = holdingStockRepository.findByCompanyAndUser(testCompany.getCompanyId(), testUser.getId());
        assertNotNull(holding);
        assertEquals(1, holding.getStockCount());

        long expectedCash = cashBefore - (price * count);
        User updatedUser = userRepository.findById(testUser.getId()).get();
        assertEquals(expectedCash, updatedUser.getCash().getMoney());
        boolean contains = testEventListener.getReceivedEvents().stream()
                .anyMatch(e -> e instanceof OrderChangedEvent);
        assertTrue(contains);
    }
}
