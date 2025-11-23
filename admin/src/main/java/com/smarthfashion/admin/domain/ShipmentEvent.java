package com.smarthfashion.admin.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "EventoEnvio")
public class ShipmentEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_envio")
    private Shipment envio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    @Column(columnDefinition = "TEXT")
    private String nota;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shipment getEnvio() { return envio; }
    public void setEnvio(Shipment envio) { this.envio = envio; }

    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }

    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
}
