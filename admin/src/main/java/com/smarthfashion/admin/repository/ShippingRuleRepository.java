package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.ShippingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShippingRuleRepository extends JpaRepository<ShippingRule, Long> {
    List<ShippingRule> findByOrigenRegionIgnoreCaseAndDestinoRegionIgnoreCaseOrderByPrioridadAsc(String origenRegion, String destinoRegion);
}
