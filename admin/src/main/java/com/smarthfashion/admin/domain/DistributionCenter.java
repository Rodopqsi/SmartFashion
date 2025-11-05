package com.smarthfashion.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "CentroDistribucion")
public class DistributionCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    // Región o ciudad donde opera el centro
    @Column(nullable = false)
    private String region;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
