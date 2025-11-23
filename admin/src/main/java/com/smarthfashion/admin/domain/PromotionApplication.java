package com.smarthfashion.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "Aplicacion_promocion")
public class PromotionApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_promocion", nullable = false)
    private Promotion promocion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Product producto; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Category categoria; 

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Promotion getPromocion() { return promocion; }
    public void setPromocion(Promotion promocion) { this.promocion = promocion; }
    public Product getProducto() { return producto; }
    public void setProducto(Product producto) { this.producto = producto; }
    public Category getCategoria() { return categoria; }
    public void setCategoria(Category categoria) { this.categoria = categoria; }
}
