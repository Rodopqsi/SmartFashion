package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    @Query("SELECT d FROM Devolucion d\n" +
	    "WHERE (:estado IS NULL OR d.estado = :estado)\n" +
	    "  AND (:q IS NULL OR LOWER(d.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.orderNumber) LIKE LOWER(CONCAT('%', :q, '%')))\n" +
	    "  AND (:desde IS NULL OR d.createdAt >= :desde)\n" +
	    "  AND (:hasta IS NULL OR d.createdAt < :hasta)\n" +
	    "ORDER BY d.id DESC")
    List<Devolucion> search(
	    @Param("estado") String estado,
	    @Param("q") String q,
	    @Param("desde") Instant desde,
	    @Param("hasta") Instant hasta
    );
}
