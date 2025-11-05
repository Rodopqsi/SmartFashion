package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long>, JpaSpecificationExecutor<Orders> {
    Optional<Orders> findByOrderNumber(String orderNumber);
}
