package com.smarthfashion.admin.report.dto;

import java.math.BigDecimal;

public class TopProductDTO {
    private final Long productId;
    private final String name;
    private final long quantity;
    private final BigDecimal revenue;

    public TopProductDTO(Long productId, String name, long quantity, BigDecimal revenue) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.revenue = revenue == null ? BigDecimal.ZERO : revenue;
    }

    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public long getQuantity() { return quantity; }
    public BigDecimal getRevenue() { return revenue; }
}
