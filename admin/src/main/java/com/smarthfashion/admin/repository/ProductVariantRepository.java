package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
	List<ProductVariant> findByProduct_Id(Long productId);
}
