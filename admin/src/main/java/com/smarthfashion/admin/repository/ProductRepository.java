package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
