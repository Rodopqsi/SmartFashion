package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.Reclamacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReclamacionRepository extends JpaRepository<Reclamacion, Long> {

    @Query("SELECT r FROM Reclamacion r\n" +
	    "WHERE (:estado IS NULL OR r.estado = :estado)\n" +
	    "  AND (:q IS NULL OR LOWER(r.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(r.orderNumber) LIKE LOWER(CONCAT('%', :q, '%')))\n" +
	    "  AND (:desde IS NULL OR r.createdAt >= :desde)\n" +
	    "  AND (:hasta IS NULL OR r.createdAt < :hasta)\n" +
	    "ORDER BY r.id DESC")
    List<Reclamacion> search(
	    @Param("estado") String estado,
	    @Param("q") String q,
	    @Param("desde") Instant desde,
	    @Param("hasta") Instant hasta
    );
}
