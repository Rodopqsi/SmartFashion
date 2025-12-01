package com.smarthfashion.admin.report;

import com.smarthfashion.admin.report.dto.SalesSummaryDTO;
import com.smarthfashion.admin.report.dto.TimeSeriesPointDTO;
import com.smarthfashion.admin.report.dto.TopProductDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesReportService {
    private final SalesReportRepository repo;

    public SalesReportService(SalesReportRepository repo) {
        this.repo = repo;
    }

    public SalesSummaryDTO todaySummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(LocalTime.MAX);
        Object[] row = repo.rangeSummary(from, to);
        return toSummary("Hoy", row);
    }

    public SalesSummaryDTO monthSummary() {
        LocalDate now = LocalDate.now();
        LocalDate first = now.withDayOfMonth(1);
        LocalDateTime from = first.atStartOfDay();
        LocalDateTime to = now.atTime(LocalTime.MAX);
        Object[] row = repo.rangeSummary(from, to);
        return toSummary("Mes", row);
    }

    public SalesSummaryDTO yearSummary() {
        LocalDate now = LocalDate.now();
        LocalDate first = now.withDayOfYear(1);
        LocalDateTime from = first.atStartOfDay();
        LocalDateTime to = now.atTime(LocalTime.MAX);
        Object[] row = repo.rangeSummary(from, to);
        return toSummary("Año", row);
    }

    public List<TimeSeriesPointDTO> last30Days() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(29); // inclusive
        List<Object[]> raw = repo.dailyTotals(start.atStartOfDay(), today.atTime(LocalTime.MAX));
        return mapSeries(raw);
    }

    public List<TimeSeriesPointDTO> last12Months() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusMonths(11).withDayOfMonth(1);
        List<Object[]> raw = repo.monthlyTotals(start.atStartOfDay(), now.atTime(LocalTime.MAX));
        return mapSeries(raw);
    }

    public List<TopProductDTO> topProductsLast30Days() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(29);
        List<Object[]> raw = repo.topProducts(start.atStartOfDay(), today.atTime(LocalTime.MAX));
        List<TopProductDTO> list = new ArrayList<>();
        for (Object[] r : raw) {
            Long productId = r[0] == null ? null : ((Number) r[0]).longValue();
            String name = (String) r[1];
            long qty = r[2] == null ? 0L : ((Number) r[2]).longValue();
            BigDecimal revenue = r[3] == null ? BigDecimal.ZERO : (r[3] instanceof BigDecimal ? (BigDecimal) r[3] : BigDecimal.valueOf(((Number) r[3]).doubleValue()));
            list.add(new TopProductDTO(productId, name, qty, revenue));
        }
        return list;
    }

    private SalesSummaryDTO toSummary(String label, Object[] row) {
        BigDecimal total = BigDecimal.ZERO;
        long orders = 0L;
        if (row != null) {
            if (row[0] instanceof BigDecimal) total = (BigDecimal) row[0];
            else if (row[0] instanceof Number) total = BigDecimal.valueOf(((Number) row[0]).doubleValue());
            if (row[1] instanceof Number) orders = ((Number) row[1]).longValue();
        }
        return new SalesSummaryDTO(label, total, orders);
    }

    private List<TimeSeriesPointDTO> mapSeries(List<Object[]> raw) {
        List<TimeSeriesPointDTO> list = new ArrayList<>();
        for (Object[] r : raw) {
            String period = String.valueOf(r[0]);
            BigDecimal total = BigDecimal.ZERO;
            long orders = 0L;
            if (r[1] instanceof BigDecimal) total = (BigDecimal) r[1];
            else if (r[1] instanceof Number) total = BigDecimal.valueOf(((Number) r[1]).doubleValue());
            if (r[2] instanceof Number) orders = ((Number) r[2]).longValue();
            list.add(new TimeSeriesPointDTO(period, total, orders));
        }
        return list;
    }
}
