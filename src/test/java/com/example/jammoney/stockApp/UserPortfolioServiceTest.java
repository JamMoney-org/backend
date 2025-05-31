package com.example.jammoney.stockApp;

import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.cash.repository.CashRepository;
import com.example.jammoney.stockApp.stock.entity.*;
import com.example.jammoney.stockApp.stock.repository.CompanyRepository;
import com.example.jammoney.stockApp.stock.repository.HoldingStockRepository;
import com.example.jammoney.stockApp.stock.repository.StockAskingPriceRepository;
import com.example.jammoney.stockApp.stock.repository.UserPortfolioRepository;
import com.example.jammoney.stockApp.stock.service.HoldingStockService;
import com.example.jammoney.stockApp.stock.service.UserPortfolioService;
import com.example.jammoney.user.Role;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class UserPortfolioServiceTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private HoldingStockRepository holdingStockRepository;
    @Autowired private UserPortfolioRepository userPortfolioRepository;
    @Autowired private UserPortfolioService userPortfolioService;
    @Autowired private CashRepository cashRepository;
    @Autowired private StockAskingPriceRepository stockAskingPriceRepository;

    private User user;
    private Company company;

    @BeforeEach
    void setup() throws Exception {
        // 1. 유저 생성
        user = User.builder()
                .email("test@port.com")
                .password("pw123")
                .nickname("testuser")
                .isActive(true)
                .holdingStocks(new ArrayList<>())
                .role(Role.ROLE_USER)
                .build();

        // 2. Cash 설정 및 연관관계
        Cash cash = new Cash();
        cash.setMoney(1_000_000L);
        cash.setUser(user);
        user.setCash(cash);

        user = userRepository.save(user);

        em.flush(); // 💡 이거 추가: Cash도 DB에 insert

        // 3. 회사 및 주가 정보
        company = new Company();
        company.setCode("000000");
        company.setKorName("테스트회사");

        StockInfo info = new StockInfo();
        info.setStck_prpr("50000");
        info.setCompany(company);
        company.setStockInfo(info);

        StockAskingPrice ask = new StockAskingPrice();
        for (int i = 1; i <= 10; i++) {
            Field askp = StockAskingPrice.class.getDeclaredField("askp" + i);
            Field askp_rsqn = StockAskingPrice.class.getDeclaredField("askp_rsqn" + i);
            Field bidp = StockAskingPrice.class.getDeclaredField("bidp" + i);
            Field bidp_rsqn = StockAskingPrice.class.getDeclaredField("bidp_rsqn" + i);

            askp.setAccessible(true); askp.set(ask, "80000");
            askp_rsqn.setAccessible(true); askp_rsqn.set(ask, "100");
            bidp.setAccessible(true); bidp.set(ask, "79000");
            bidp_rsqn.setAccessible(true); bidp_rsqn.set(ask, "200");
        }
        ask.setCompany(company);
        stockAskingPriceRepository.save(ask);
        company.setStockAskingPrice(ask);

        company = companyRepository.save(company);

        // 4. HoldingStock (양방향 관계 설정)
        HoldingStock stock = HoldingStock.builder()
                .user(user)
                .company(company)
                .stockCount(10)
                .totalPrice(600_000L)
                .build();
        stock.setUser(user); // 명시적으로 설정
        stock.setCompany(company); // 명시적으로 설정
        user.getHoldingStocks().add(stock);
        holdingStockRepository.save(stock);

        // 5. UserPortfolio 저장
        UserPortfolio portfolio = new UserPortfolio();
        portfolio.setUser(user);
        userPortfolioRepository.save(portfolio);

        em.flush();
        em.clear();
    }


    @Test
    void 매수_주문후_포트폴리오_수익률_정확히_계산되는지_확인() {
        // when
        userPortfolioService.updateUserPortfolio(user);

        // then
        UserPortfolio result = userPortfolioRepository.findByUser(user);
        long expectedStockAsset = 500_000L;
        long expectedTotalAsset = 1_000_000L + expectedStockAsset;
        long expectedProfitAmount = expectedStockAsset - 600_000L;
        double expectedProfitRate = (expectedProfitAmount / (double) 600_000L) * 100;

        assertEquals(1_000_000L, result.getUser().getCash().getMoney());
        assertEquals(expectedStockAsset, result.getStockAsset());
        assertEquals(expectedTotalAsset, result.getTotalAsset());
        assertEquals(expectedProfitAmount, result.getProfitAmount());
        assertEquals(expectedProfitRate, result.getProfitRate(), 0.01);
    }

    @Test
    void 현금만_변동_후_포트폴리오_갱신되는지_확인() {
        // given
        user.getCash().setMoney(2_000_000L);
        userRepository.save(user);

        // when
        userPortfolioService.updateUserPortfolio(user);

        // then
        UserPortfolio result = userPortfolioRepository.findByUser(user);
        assertEquals(2_000_000L + 500_000L, result.getTotalAsset());
        assertEquals(2_000_000L, result.getUser().getCash().getMoney());
    }

    @Test
    void 전체_포트폴리오_업데이트_정확성_검증() {
        // when
        userPortfolioService.updateAllUserPortfolios();

        // then
        UserPortfolio result = userPortfolioRepository.findByUser(user);
        long expectedStockAsset = 500_000L;
        long expectedTotalAsset = 1_000_000L + expectedStockAsset;

        assertEquals(expectedStockAsset, result.getStockAsset());
        assertEquals(expectedTotalAsset, result.getTotalAsset());
    }
}
