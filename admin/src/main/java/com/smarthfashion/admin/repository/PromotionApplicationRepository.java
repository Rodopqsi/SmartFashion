package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.PromotionApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionApplicationRepository extends JpaRepository<PromotionApplication, Long> {
    List<PromotionApplication> findByPromocionId(Long promoId);
}
