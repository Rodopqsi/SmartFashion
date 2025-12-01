package com.smarthfashion.admin.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TimeSeriesPointDTO {
    private final String period; // e.g. YYYY-MM-DD or YYYY-MM
    private final BigDecimal total;
    private final long orders;

    public TimeSeriesPointDTO(String period, BigDecimal total, long orders) {
        this.period = period;
        this.total = total == null ? BigDecimal.ZERO : total;
        this.orders = orders;
    }

    public String getPeriod() { return period; }
    public BigDecimal getTotal() { return total; }
    public long getOrders() { return orders; }
}
