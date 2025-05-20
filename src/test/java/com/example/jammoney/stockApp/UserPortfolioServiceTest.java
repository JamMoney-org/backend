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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class UserPortfolioServiceTest {
    @PersistenceContext
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private HoldingStockRepository holdingStockRepository;
    @Autowired
    private UserPortfolioRepository userPortfolioRepository;
    @Autowired
    private UserPortfolioService userPortfolioService;
    @Autowired
    private CashRepository cashRepository;
    @Autowired
    private StockAskingPriceRepository stockAskingPriceRepository;

    private User user;
    private Company company;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        // 유저 생성
        user = new User();
        user.setEmail("test@port.com");
        user.setPassword("pwsefsefs#");
        user.setNickname("testuser");

        Cash cash = new Cash();
        cash.setMoney(1_000_000L);

// 양방향 연관관계 설정
        cash.setUser(user);
        user.setCash(cash);

// 🔥 cash 저장하지 않고 user만 저장해도 연관관계 자동 저장
        user = userRepository.save(user);
        System.out.println("User Cash 연결 확인: " + userRepository.findById(user.getId()).get().getCash());

        // 회사 및 주가 정보
        company = new Company();
        company.setCode("000000");
        company.setKorName("테스트회사");

        StockInfo info = new StockInfo();
        info.setCompany(company);
        info.setStck_prpr("50000");  // 현재가: 5만원
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
        companyRepository.save(company);

        // 보유 주식: 10주 * 매입가 6만원 = 60만원 투자
        HoldingStock stock = new HoldingStock();
        stock.setCompany(company);
        stock.setUser(user);
        stock.setStockCount(10);
        stock.setTotalPrice(600_000L);  // 총 매입 금액
        stockAskingPriceRepository.save(ask);
        company.setStockAskingPrice(ask);
        holdingStockRepository.save(stock);
        em.flush();
        em.clear();

        // 사용자 포트폴리오 생성
        UserPortfolio portfolio = new UserPortfolio();
        portfolio.setUser(user);
        userPortfolioRepository.save(portfolio);
    }

    @Test
    void 포트폴리오_수익률_정확히_계산되는지_확인() {
        // when

        userPortfolioService.updateAllUserPortfolios();

        // then
        UserPortfolio result = userPortfolioRepository.findByUser(user);

        long expectedStockAsset = 500_000L; // 현재가 5만원 * 10주
        long expectedTotalAsset = 1_000_000L + expectedStockAsset; // 현금 + 주식자산
        long expectedProfitAmount = expectedTotalAsset - (1_000_000L + 600_000L); // 총자산 - (현금 + 투자원금)
        double expectedProfitRate = (expectedProfitAmount / (double) 600_000L) * 100;

        assertEquals(1_000_000L, result.getCash());
        assertEquals(expectedStockAsset, result.getStockAsset());
        assertEquals(expectedTotalAsset, result.getTotalAsset());
        assertEquals(expectedProfitAmount, result.getProfitAmount());
        assertEquals(expectedProfitRate, result.getProfitRate(), 0.01); // 소수점 비교
    }
}
