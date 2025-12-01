package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.Shipment;
import com.smarthfashion.admin.repository.ShipmentEventRepository;
import com.smarthfashion.admin.repository.ShipmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tracking/api")
public class TrackingApiController {
    private final ShipmentRepository shipmentRepo;
    private final ShipmentEventRepository eventRepo;

    public TrackingApiController(ShipmentRepository shipmentRepo, ShipmentEventRepository eventRepo) {
        this.shipmentRepo = shipmentRepo;
        this.eventRepo = eventRepo;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getTracking(@PathVariable String orderId) {
        Shipment s = shipmentRepo.findFirstByOrderIdOrderByCreadoEnDesc(orderId).orElse(null);
        if (s == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", s.getOrderId());
        payload.put("status", s.getStatus() != null ? s.getStatus().name() : null);
        payload.put("empresa", s.getEmpresaEnvio() != null ? s.getEmpresaEnvio().getNombre() : null);
        payload.put("codigoTracking", s.getCodigoTracking());
        payload.put("email", s.getEmailDestino());
        payload.put("telefono", s.getTelefonoDestino());
        List<Map<String, Object>> events = eventRepo.findByEnvioOrderByFechaAsc(s).stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("status", e.getStatus() != null ? e.getStatus().name() : null);
            m.put("nota", e.getNota());
            m.put("fecha", e.getFecha());
            return m;
        }).collect(Collectors.toList());
        payload.put("events", events);
        return ResponseEntity.ok(payload);
    }
}
