package com.example.jammoney.stockApp;

import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.stockApp.stock.entity.Company;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderStatus;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderType;
import com.example.jammoney.stockApp.stock.entity.HoldingStock;
import com.example.jammoney.stockApp.stock.entity.Order;
import com.example.jammoney.stockApp.stock.entity.StockAskingPrice;
import com.example.jammoney.stockApp.stock.repository.CompanyRepository;
import com.example.jammoney.stockApp.stock.repository.HoldingStockRepository;
import com.example.jammoney.stockApp.stock.repository.StockAskingPriceRepository;
import com.example.jammoney.stockApp.stock.service.CompanyService;
import com.example.jammoney.stockApp.stock.service.HoldingStockService;
import com.example.jammoney.stockApp.stock.service.OrderService;
import com.example.jammoney.stockApp.stock.service.StockAskingPriceService;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class orderServiceTest {
    @Autowired
    OrderService orderService;
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
        userRepository.save(testUser);

        // 2. 회사 생성
        Company company = new Company();
        company.setCode("005930");
        company.setKorName("삼성전자");

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
}
