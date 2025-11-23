package com.smarthfashion.admin.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ReglaEnvio")
public class ShippingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "origen_region", nullable = false)
    private String origenRegion;

    
    @Column(name = "destino_region", nullable = false)
    private String destinoRegion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa_envio")
    private ShippingCompany empresaEnvio;

    
    @Column(nullable = false)
    private int prioridad = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costo = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrigenRegion() { return origenRegion; }
    public void setOrigenRegion(String origenRegion) { this.origenRegion = origenRegion; }

    public String getDestinoRegion() { return destinoRegion; }
    public void setDestinoRegion(String destinoRegion) { this.destinoRegion = destinoRegion; }

    public ShippingCompany getEmpresaEnvio() { return empresaEnvio; }
    public void setEmpresaEnvio(ShippingCompany empresaEnvio) { this.empresaEnvio = empresaEnvio; }

    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }

    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
}
