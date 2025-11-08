package com.example.jammoney.cash.service;

import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.cash.repository.CashRepository;
import com.example.jammoney.exception.CashNotFoundException;
import com.example.jammoney.exception.InsufficientBalanceException;
import com.example.jammoney.exception.UserNotFoundException;
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

    /* ================ 생성/기본 ================ */

    @Transactional
    public void createCash(Long userId, long initialAmount) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.getCash() != null) throw new IllegalStateException("이미 Cash 정보가 존재합니다.");

        Cash cash = new Cash();
        cash.setMoney(initialAmount);
        cash.setReservedCash(0L);
        cash.setUser(user);
        user.setCash(cash); // 양방향 매핑

        cashRepository.save(cash); // 신규 생성은 save
    }

    @Transactional
    public void addCash(long userId, long amount) {
        Cash cash = cashRepository.findByUserId(userId).orElseThrow(CashNotFoundException::new);
        if (amount < 0) throw new IllegalArgumentException("추가되는 금액은 무조건 양수여야 합니다.");
        cash.increase(amount); // 더티체킹
    }

    @Transactional
    public void subtractCash(Long userId, long amount) {
        Cash cash = cashRepository.findByUserId(userId).orElseThrow(CashNotFoundException::new);
        if (amount < 0) throw new IllegalArgumentException("차감되는 금액은 무조건 음수여야 합니다.");
        if (cash.getMoney() < amount) throw new InsufficientBalanceException();
        cash.decrease(amount);
    }

    public long getMoney(Long userId) {
        return cashRepository.findByUserId(userId).map(Cash::getMoney).orElse(0L);
    }

    /** 가용 현금으로만 검사 */
    public void checkCash(long price, User user) {
        Cash cash = user.getCash();
        if (cash == null) throw new CashNotFoundException();
        if (price > cash.available()) throw new InsufficientBalanceException();
    }

    /* ================ 예약금 플로우 ================ */

    /** 사용 가능 현금으로 충분한지 검증만 */
    public void assertAvailable(long amount, User user) {
        Cash cash = user.getCash();
        if (cash == null) throw new CashNotFoundException();
        if (amount < 0) throw new IllegalArgumentException("비용은 무조건 0 이상이어야 합니다.");
        if (cash.available() < amount) throw new InsufficientBalanceException();
    }

    /** BUY 예약 생성 시: 가용 현금을 예약금으로 이동  */
    @Transactional
    public void reserve(long amount, User user) {
        Cash cash = user.getCash();
        if (cash == null) throw new CashNotFoundException();
        if (amount < 0) throw new IllegalArgumentException("예약금은 무조건 0 이상이어야 합니다.");
        if (cash.available() < amount) throw new InsufficientBalanceException();
        cash.reserve(amount);
    }

    /** 예약 취소 시: 예약금 환원  */
    @Transactional
    public void releaseReserved(long amount, User user) {
        Cash cash = user.getCash();
        if (cash == null) throw new CashNotFoundException();
        if (amount < 0) throw new IllegalArgumentException("취소금액은 무조건 0 이상이어야 합니다.");
        if (cash.getReservedCash() < amount) throw new IllegalStateException("예약 취소 금액이 예약금보다 클 수 없습니다.");
        cash.releaseReserved(amount);
    }

    /**
     * 예약 체결 시: 예약금 → 실차감 전환
     * reservedCash -= amount, money -= amount
     */
    @Transactional
    public void commitReservedForBuy(long amount, User user) {
        Cash cash = user.getCash();
        if (cash == null) throw new CashNotFoundException();
        if (amount < 0) throw new IllegalArgumentException("체결 금액은 무조건 0 이상이어어야 합니다.");

        // 이 시점에 예약금 부족/잔액 부족이 발생하면 예외 발생
        cash.commitReserved(amount);
    }

    /* ================ 편의 조회 ================ */

    public long getReserved(Long userId) {
        return cashRepository.findByUserId(userId).map(Cash::getReservedCash).orElse(0L);
    }

    public long getAvailable(Long userId) {
        return cashRepository.findByUserId(userId).map(Cash::available).orElse(0L);
    }
}
