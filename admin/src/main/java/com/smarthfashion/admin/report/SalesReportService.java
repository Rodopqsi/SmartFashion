package com.smarthfashion.admin.report;

import com.smarthfashion.admin.report.dto.SalesSummaryDTO;
import com.smarthfashion.admin.report.dto.TimeSeriesPointDTO;
import com.smarthfashion.admin.report.dto.TopProductDTO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesReportService {
    private static final Logger log = LoggerFactory.getLogger(SalesReportService.class);
    private final SalesReportRepository repo;

    public SalesReportService(SalesReportRepository repo) {
        this.repo = repo;
    }

    public SalesSummaryDTO todaySummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(LocalTime.MAX);
        try {
            List<Object[]> rows = repo.rangeSummary(from, to);
            Object[] row = (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
            return toSummary("Hoy", row);
        } catch (Exception e) {
            log.warn("SalesReport todaySummary failed: {}", e.getMessage());
            return new SalesSummaryDTO("Hoy", BigDecimal.ZERO, 0);
        }
    }

    public SalesSummaryDTO monthSummary() {
        LocalDate now = LocalDate.now();
        LocalDate first = now.withDayOfMonth(1);
        LocalDateTime from = first.atStartOfDay();
        LocalDateTime to = now.atTime(LocalTime.MAX);
        try {
            List<Object[]> rows = repo.rangeSummary(from, to);
            Object[] row = (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
            return toSummary("Mes", row);
        } catch (Exception e) {
            log.warn("SalesReport monthSummary failed: {}", e.getMessage());
            return new SalesSummaryDTO("Mes", BigDecimal.ZERO, 0);
        }
    }

    public SalesSummaryDTO yearSummary() {
        LocalDate now = LocalDate.now();
        LocalDate first = now.withDayOfYear(1);
        LocalDateTime from = first.atStartOfDay();
        LocalDateTime to = now.atTime(LocalTime.MAX);
        try {
            List<Object[]> rows = repo.rangeSummary(from, to);
            Object[] row = (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
            return toSummary("Año", row);
        } catch (Exception e) {
            log.warn("SalesReport yearSummary failed: {}", e.getMessage());
            return new SalesSummaryDTO("Año", BigDecimal.ZERO, 0);
        }
    }

    public List<TimeSeriesPointDTO> last30Days() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(29); // inclusive
        try {
            List<Object[]> raw = repo.dailyTotals(start.atStartOfDay(), today.atTime(LocalTime.MAX));
            return mapSeries(raw);
        } catch (Exception e) {
            log.warn("SalesReport last30Days failed: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public List<TimeSeriesPointDTO> last12Months() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusMonths(11).withDayOfMonth(1);
        try {
            List<Object[]> raw = repo.monthlyTotals(start.atStartOfDay(), now.atTime(LocalTime.MAX));
            return mapSeries(raw);
        } catch (Exception e) {
            log.warn("SalesReport last12Months failed: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public List<TopProductDTO> topProductsLast30Days() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(29);
        try {
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
        } catch (Exception e) {
            log.warn("SalesReport topProductsLast30Days failed: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    // Range-capable endpoints used by controller
    public List<TimeSeriesPointDTO> dailyBetween(String fromStr, String toStr) {
        LocalDateTime[] r = parseRangeOrDefault(fromStr, toStr, 29);
        try {
            List<Object[]> raw = repo.dailyTotals(r[0], r[1]);
            return mapSeries(raw);
        } catch (Exception e) {
            log.warn("SalesReport dailyBetween failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TimeSeriesPointDTO> monthlyBetween(String fromStr, String toStr) {
        LocalDateTime[] r = parseRangeOrDefaultMonths(fromStr, toStr, 11);
        try {
            List<Object[]> raw = repo.monthlyTotals(r[0], r[1]);
            return mapSeries(raw);
        } catch (Exception e) {
            log.warn("SalesReport monthlyBetween failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TopProductDTO> topProductsBetween(String fromStr, String toStr) {
        LocalDateTime[] r = parseRangeOrDefault(fromStr, toStr, 29);
        try {
            List<Object[]> raw = repo.topProducts(r[0], r[1]);
            List<TopProductDTO> list = new ArrayList<>();
            for (Object[] o : raw) {
                Long productId = o[0] == null ? null : ((Number) o[0]).longValue();
                String name = (String) o[1];
                long qty = o[2] == null ? 0L : ((Number) o[2]).longValue();
                BigDecimal revenue = o[3] == null ? BigDecimal.ZERO : (o[3] instanceof BigDecimal ? (BigDecimal) o[3] : BigDecimal.valueOf(((Number) o[3]).doubleValue()));
                list.add(new TopProductDTO(productId, name, qty, revenue));
            }
            return list;
        } catch (Exception e) {
            log.warn("SalesReport topProductsBetween failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public String exportCsv(String fromStr, String toStr) {
        LocalDateTime[] r = parseRangeOrDefault(fromStr, toStr, 29);
        StringBuilder sb = new StringBuilder();
        sb.append("period,total,orders\n");
        try {
            for (Object[] o : repo.dailyTotals(r[0], r[1])) {
                String period = String.valueOf(o[0]);
                BigDecimal total = o[1] instanceof BigDecimal ? (BigDecimal) o[1] : BigDecimal.valueOf(((Number) o[1]).doubleValue());
                long orders = ((Number) o[2]).longValue();
                sb.append(period).append(',').append(total).append(',').append(orders).append('\n');
            }
        } catch (Exception e) {
            log.warn("SalesReport exportCsv failed: {}", e.getMessage());
        }
        return sb.toString();
    }

    private LocalDateTime[] parseRangeOrDefault(String fromStr, String toStr, int defaultDaysBack) {
        try {
            LocalDate fromD = (fromStr == null || fromStr.isBlank()) ? LocalDate.now().minusDays(defaultDaysBack) : LocalDate.parse(fromStr);
            LocalDate toD = (toStr == null || toStr.isBlank()) ? LocalDate.now() : LocalDate.parse(toStr);
            return new LocalDateTime[]{fromD.atStartOfDay(), toD.atTime(LocalTime.MAX)};
        } catch (Exception e) {
            LocalDate today = LocalDate.now();
            return new LocalDateTime[]{today.minusDays(defaultDaysBack).atStartOfDay(), today.atTime(LocalTime.MAX)};
        }
    }

    private LocalDateTime[] parseRangeOrDefaultMonths(String fromStr, String toStr, int defaultMonthsBack) {
        try {
            LocalDate fromD = (fromStr == null || fromStr.isBlank()) ? LocalDate.now().minusMonths(defaultMonthsBack).withDayOfMonth(1) : LocalDate.parse(fromStr).withDayOfMonth(1);
            LocalDate toD = (toStr == null || toStr.isBlank()) ? LocalDate.now() : LocalDate.parse(toStr);
            return new LocalDateTime[]{fromD.atStartOfDay(), toD.atTime(LocalTime.MAX)};
        } catch (Exception e) {
            LocalDate now = LocalDate.now();
            return new LocalDateTime[]{now.minusMonths(defaultMonthsBack).withDayOfMonth(1).atStartOfDay(), now.atTime(LocalTime.MAX)};
        }
    }

    // Exposed for quick diagnostics in controller or logs
    public void logBasicDiagnostics() {
        try {
            Long orders = repo.countOrders();
            Long items = repo.countOrderItems();
            log.info("SalesReport diagnostics: orders={}, order_items={}", orders, items);
        } catch (Exception e) {
            log.warn("SalesReport diagnostics failed: {}", e.getMessage());
        }
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
