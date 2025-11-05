package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.ShipmentEvent;
import com.smarthfashion.admin.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentEventRepository extends JpaRepository<ShipmentEvent, Long> {
    List<ShipmentEvent> findByEnvioOrderByFechaAsc(Shipment envio);
}
