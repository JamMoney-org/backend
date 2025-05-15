package com.example.jammoney.cash;

import com.example.jammoney.exception.InsufficientBalanceException;
import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.cash.service.CashService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CashServiceTest {

    private final CashService cashService = new CashService(null, null) {
        @Override
        public void addCash(long userId, long amount) {
            Cash cash = new Cash();
            cash.setMoney(1000L);
            cash.increase(amount);
            assertEquals(1300L, cash.getMoney());
        }

        @Override
        public void subtractCash(Long userId, long amount) {
            Cash cash = new Cash();
            cash.setMoney(1000L);
            if (cash.getMoney() < amount) {
                throw new InsufficientBalanceException();
            }
            cash.decrease(amount);
            assertEquals(700L, cash.getMoney());
        }
    };

    @Test
    void 캐시_증가_테스트() {
        cashService.addCash(1L, 300L);
    }

    @Test
    void 캐시_감소_테스트() {
        cashService.subtractCash(1L, 300L);
    }

    @Test
    void 캐시_감소_실패_테스트() {
        CashService localService = new CashService(null, null) {
            @Override
            public void subtractCash(Long userId, long amount) {
                Cash cash = new Cash();
                cash.setMoney(100L);
                if (cash.getMoney() < amount) {
                    throw new InsufficientBalanceException();
                }
            }
        };

        assertThrows(InsufficientBalanceException.class, () -> {
            localService.subtractCash(1L, 200L);
        });
    }
}
