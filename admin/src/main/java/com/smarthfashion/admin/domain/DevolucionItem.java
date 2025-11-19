package com.smarthfashion.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "DevolucionItem")
public class DevolucionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devolucion_id", nullable = false)
    private Devolucion devolucion;

    @Column(name = "product_sku", length = 64)
    private String productSku;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "quantity")
    private Integer quantity = 1;

    @Column(name = "condicion", length = 32)
    private String condicion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Devolucion getDevolucion() { return devolucion; }
    public void setDevolucion(Devolucion devolucion) { this.devolucion = devolucion; }
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getCondicion() { return condicion; }
    public void setCondicion(String condicion) { this.condicion = condicion; }
}
