package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findFirstByOrderIdOrderByCreadoEnDesc(String orderId);
}
