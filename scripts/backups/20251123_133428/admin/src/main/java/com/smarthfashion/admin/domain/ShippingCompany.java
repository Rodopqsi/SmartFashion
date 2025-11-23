package com.smarthfashion.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "EmpresaEnvio")
public class ShippingCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    // Región/cobertura principal (ej: "Lima", "Nacional", "Internacional")
    @Column(nullable = false)
    private String cobertura;

    // Campo opcional para URL de tracking externo
    @Column(name = "tracking_url_base")
    private String trackingUrlBase;

    // Activo/inactivo para asignación automática
    @Column(nullable = false)
    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCobertura() { return cobertura; }
    public void setCobertura(String cobertura) { this.cobertura = cobertura; }

    public String getTrackingUrlBase() { return trackingUrlBase; }
    public void setTrackingUrlBase(String trackingUrlBase) { this.trackingUrlBase = trackingUrlBase; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
