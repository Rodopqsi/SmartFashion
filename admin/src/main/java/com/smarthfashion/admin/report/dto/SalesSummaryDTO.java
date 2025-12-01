package com.smarthfashion.admin.report.dto;

import java.math.BigDecimal;

public class SalesSummaryDTO {
    private final BigDecimal totalSales;
    private final long ordersCount;
    private final String label;

    public SalesSummaryDTO(String label, BigDecimal totalSales, long ordersCount) {
        this.label = label;
        this.totalSales = totalSales == null ? BigDecimal.ZERO : totalSales;
        this.ordersCount = ordersCount;
    }

    public BigDecimal getTotalSales() { return totalSales; }
    public long getOrdersCount() { return ordersCount; }
    public String getLabel() { return label; }
}
