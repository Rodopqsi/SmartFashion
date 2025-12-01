package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p where " +
	    "(:q is null or lower(p.nombre) like lower(concat('%', :q, '%')) or lower(p.descripcion) like lower(concat('%', :q, '%'))) and " +
	    "(:categoriaId is null or p.categoria.id = :categoriaId) and " +
	    "(:precioMin is null or p.precio >= :precioMin) and " +
	    "(:precioMax is null or p.precio <= :precioMax) " +
	    "order by p.id desc")
    List<Product> search(
	    @Param("q") String q,
	    @Param("categoriaId") Long categoriaId,
	    @Param("precioMin") BigDecimal precioMin,
	    @Param("precioMax") BigDecimal precioMax
    );
}
