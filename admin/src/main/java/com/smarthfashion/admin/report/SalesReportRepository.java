package com.smarthfashion.admin.report;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// Read-only repository for aggregated sales reporting using existing tables.
public interface SalesReportRepository extends Repository<com.smarthfashion.admin.domain.Orders, Long> {

    // Daily totals between range (inclusive)
    @Query(value = "SELECT DATE(created_at) as d, COALESCE(SUM(total),0) as total, COUNT(id) as orders FROM orders WHERE created_at BETWEEN :from AND :to GROUP BY DATE(created_at) ORDER BY d", nativeQuery = true)
    List<Object[]> dailyTotals(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Monthly totals between range (inclusive months)
    @Query(value = "SELECT DATE_FORMAT(created_at,'%Y-%m') as m, COALESCE(SUM(total),0) as total, COUNT(id) as orders FROM orders WHERE created_at BETWEEN :from AND :to GROUP BY DATE_FORMAT(created_at,'%Y-%m') ORDER BY m", nativeQuery = true)
    List<Object[]> monthlyTotals(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Summary for a date range
    @Query(value = "SELECT COALESCE(SUM(total),0) as total, COUNT(*) as orders FROM orders WHERE created_at BETWEEN :from AND :to", nativeQuery = true)
    List<Object[]> rangeSummary(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Top products within a date range (by quantity, limit 10)
    @Query(value = "SELECT oi.product_id, MAX(oi.name) as name, COALESCE(SUM(oi.qty),0) as qty, COALESCE(SUM(oi.amount),0) as revenue " +
            "FROM order_items oi JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.created_at BETWEEN :from AND :to " +
            "GROUP BY oi.product_id ORDER BY qty DESC LIMIT 10", nativeQuery = true)
    List<Object[]> topProducts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Basic diagnostics: row counts
    @Query(value = "SELECT COUNT(*) FROM orders", nativeQuery = true)
    Long countOrders();

    @Query(value = "SELECT COUNT(*) FROM order_items", nativeQuery = true)
    Long countOrderItems();
}
