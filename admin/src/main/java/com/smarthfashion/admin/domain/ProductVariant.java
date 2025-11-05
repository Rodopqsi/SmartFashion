package com.smarthfashion.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "variaciones_producto")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_talla")
    private Size size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_color")
    private Color color;

    @Column(nullable = false)
    private Integer stock = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Size getSize() { return size; }
    public void setSize(Size size) { this.size = size; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
