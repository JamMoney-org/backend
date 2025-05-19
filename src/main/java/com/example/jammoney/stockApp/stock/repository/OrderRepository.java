package com.example.jammoney.stockApp.stock.repository;

import com.example.jammoney.stockApp.stock.entity.Enums.OrderStatus;
import com.example.jammoney.stockApp.stock.entity.Enums.OrderType;
import com.example.jammoney.stockApp.stock.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByCompany_CompanyIdAndOrderStatus(Long company_companyId, OrderStatus orderStatus);
    List<Order> findAllByUser_Id(Long userId);
    List<Order> findAllByUser_IdOrderByModifiedAtDesc(Long userId);
    List<Order> findAllByUser_IdAndCompany_CompanyIdAndOrderStatusAndOrderType(Long userId, long companyId, OrderStatus orderStatus, OrderType orderType);

    List<Order> findByUserId(Long userId);
}