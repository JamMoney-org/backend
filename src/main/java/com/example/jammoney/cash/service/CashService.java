package com.example.jammoney.cash.service;

import com.example.jammoney.exception.CashNotFoundException;
import com.example.jammoney.exception.InsufficientBalanceException;
import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.cash.repository.CashRepository;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CashService {

    private final CashRepository cashRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createCash(Long userId, long initialAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getCash() != null) {
            throw new IllegalStateException("이미 Cash 정보가 존재합니다.");
        }

        Cash cash = new Cash();
        cash.setMoney(initialAmount);
        cash.setUser(user);
        user.setCash(cash); // 양방향 매핑

        cashRepository.save(cash);
    }
    @Transactional
    public void addCash(long userId, long amount) {
        Cash cash = cashRepository.findByUserId(userId)
                .orElseThrow(CashNotFoundException::new);
        cash.increase(amount);
    }

    @Transactional
    public void subtractCash(Long userId, long amount) {
        Cash cash = cashRepository.findByUserId(userId)
                .orElseThrow(CashNotFoundException::new);
        if (cash.getMoney() < amount) {
            throw new InsufficientBalanceException();
        }
        cash.decrease(amount);
    }

    public long getMoney(Long userId) {
        return cashRepository.findByUserId(userId)
                .map(Cash::getMoney)
                .orElse(0L);
    }
}
