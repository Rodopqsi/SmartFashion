package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.ShippingCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShippingCompanyRepository extends JpaRepository<ShippingCompany, Long> {
    List<ShippingCompany> findByActivoTrueOrderByNombreAsc();
    List<ShippingCompany> findByCoberturaIgnoreCase(String cobertura);
}
