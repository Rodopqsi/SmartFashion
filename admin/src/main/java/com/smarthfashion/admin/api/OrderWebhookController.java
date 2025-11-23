package com.smarthfashion.admin.api;

import com.smarthfashion.admin.domain.*;
import com.smarthfashion.admin.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal")
public class OrderWebhookController {

    private final DistributionCenterRepository centerRepo;
    private final ShippingRuleRepository ruleRepo;
    private final ShippingCompanyRepository companyRepo;
    private final ShipmentRepository shipmentRepo;
    private final ShipmentEventRepository eventRepo;

    @Value("${webhook.shared-secret:}")
    private String sharedSecret;

    public OrderWebhookController(DistributionCenterRepository centerRepo,
                                  ShippingRuleRepository ruleRepo,
                                  ShippingCompanyRepository companyRepo,
                                  ShipmentRepository shipmentRepo,
                                  ShipmentEventRepository eventRepo) {
        this.centerRepo = centerRepo;
        this.ruleRepo = ruleRepo;
        this.companyRepo = companyRepo;
        this.shipmentRepo = shipmentRepo;
        this.eventRepo = eventRepo;
    }

    
    
    
    @PostMapping("/orders")
    public ResponseEntity<?> createShipmentForOrder(
            @RequestHeader(value = "X-Webhook-Token", required = false) String token,
            @RequestBody Map<String, Object> payload) {

        if (StringUtils.hasText(sharedSecret)) {
            if (!StringUtils.hasText(token) || !sharedSecret.equals(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
            }
        }

        String orderNumber = str(payload.get("orderNumber"));
        String destinatario = str(payload.get("destinatario"));
        String direccion = str(payload.get("direccion"));
        String regionDestino = str(payload.get("regionDestino"));
    String centroRegion = str(payload.get("centroRegion"));
    String email = str(payload.get("email"));
    String telefono = str(payload.get("telefono"));

        if (!StringUtils.hasText(orderNumber) || !StringUtils.hasText(destinatario)
                || !StringUtils.hasText(direccion) || !StringUtils.hasText(regionDestino)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }

        DistributionCenter center = null;
        List<DistributionCenter> all = centerRepo.findAll();
        if (StringUtils.hasText(centroRegion)) {
            for (DistributionCenter c : all) {
                if (c.getRegion() != null && c.getRegion().equalsIgnoreCase(centroRegion)) { center = c; break; }
            }
        }
        if (center == null && !all.isEmpty()) center = all.get(0);
        if (center == null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(Map.of("error", "No distribution centers configured"));
        }

    Shipment s = new Shipment();
        s.setOrderId(orderNumber);
        s.setCentroDistribucion(center);
        s.setDestinatario(destinatario);
        s.setDireccion(direccion);
        s.setRegionDestino(regionDestino);
    s.setEmailDestino(email);
    s.setTelefonoDestino(telefono);
        s.setStatus(ShipmentStatus.CREADO);

        
        List<ShippingRule> rules = ruleRepo.findByOrigenRegionIgnoreCaseAndDestinoRegionIgnoreCaseOrderByPrioridadAsc(center.getRegion(), regionDestino);
        if (!rules.isEmpty()) {
            ShippingRule r = rules.get(0);
            s.setEmpresaEnvio(r.getEmpresaEnvio());
            s.setCostoEnvio(r.getCosto());
            s.setStatus(ShipmentStatus.ASIGNADO);
        } else {
            List<ShippingCompany> candidates = companyRepo.findByCoberturaIgnoreCase(regionDestino);
            if (candidates.isEmpty()) candidates = companyRepo.findByActivoTrueOrderByNombreAsc();
            if (!candidates.isEmpty()) {
                s.setEmpresaEnvio(candidates.get(0));
                s.setCostoEnvio(null);
                s.setStatus(ShipmentStatus.ASIGNADO);
            }
        }

        Shipment saved = shipmentRepo.save(s);

    ShipmentEvent ev = new ShipmentEvent();
        ev.setEnvio(saved);
        ev.setStatus(saved.getStatus());
    StringBuilder note = new StringBuilder("Envío creado desde checkout");
    if (email != null && !email.isBlank()) note.append(" · ").append(email);
    if (telefono != null && !telefono.isBlank()) note.append(" · ").append(telefono);
    ev.setNota(note.toString());
        eventRepo.save(ev);

        String trackingUrl = "/tracking/" + saved.getOrderId();
        return ResponseEntity.created(URI.create("/admin/shipments/" + saved.getId()))
                .body(Map.of(
                        "shipmentId", saved.getId(),
                        "orderNumber", saved.getOrderId(),
                        "status", saved.getStatus().name(),
                        "trackingUrl", trackingUrl
                ));
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o).trim(); }
}
