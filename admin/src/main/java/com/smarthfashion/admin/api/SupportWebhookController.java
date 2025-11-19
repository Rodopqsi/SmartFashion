package com.smarthfashion.admin.api;

import com.smarthfashion.admin.domain.Devolucion;
import com.smarthfashion.admin.domain.Reclamacion;
import com.smarthfashion.admin.repository.DevolucionRepository;
import com.smarthfashion.admin.repository.ReclamacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/internal")
public class SupportWebhookController {

    private final ReclamacionRepository reclamoRepo;
    private final DevolucionRepository devolucionRepo;

    @Value("${webhook.shared-secret:}")
    private String sharedSecret;

    public SupportWebhookController(ReclamacionRepository reclamoRepo, DevolucionRepository devolucionRepo) {
        this.reclamoRepo = reclamoRepo;
        this.devolucionRepo = devolucionRepo;
    }

    @PostMapping("/claims")
    public ResponseEntity<?> receiveClaim(
            @RequestHeader(value = "X-Webhook-Token", required = false) String token,
            @RequestBody Map<String, Object> payload) {
        if (StringUtils.hasText(sharedSecret)) {
            if (!StringUtils.hasText(token) || !sharedSecret.equals(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
            }
        }
        String orderNumber = str(payload.get("orderNumber"));
        String email = str(payload.get("email"));
        String telefono = str(payload.get("telefono"));
        String tipo = str(payload.get("tipo"));
        String detalle = str(payload.get("detalle"));
        if (!StringUtils.hasText(orderNumber) || !StringUtils.hasText(email) || !StringUtils.hasText(tipo) || !StringUtils.hasText(detalle)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }
        Reclamacion r = new Reclamacion();
        r.setOrderNumber(orderNumber);
        r.setEmail(email);
        r.setTelefono(telefono);
        r.setTipo(tipo);
        r.setDetalle(detalle);
        r.setEstado("registrado");
        Reclamacion saved = reclamoRepo.save(r);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "status", saved.getEstado()));
    }

    @PostMapping("/returns")
    public ResponseEntity<?> receiveReturn(
            @RequestHeader(value = "X-Webhook-Token", required = false) String token,
            @RequestBody Map<String, Object> payload) {
        if (StringUtils.hasText(sharedSecret)) {
            if (!StringUtils.hasText(token) || !sharedSecret.equals(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
            }
        }
        String orderNumber = str(payload.get("orderNumber"));
        String email = str(payload.get("email"));
        String telefono = str(payload.get("telefono"));
        String motivo = str(payload.get("motivo"));
        String descripcion = str(payload.get("descripcion"));
        String metodo = str(payload.get("metodo"));
        if (!StringUtils.hasText(orderNumber) || !StringUtils.hasText(email) || !StringUtils.hasText(motivo) || !StringUtils.hasText(metodo)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }
        Devolucion d = new Devolucion();
        d.setOrderNumber(orderNumber);
        d.setEmail(email);
        d.setTelefono(telefono);
        d.setMotivo(motivo);
        d.setDescripcion(descripcion);
        d.setMetodo(metodo);
        d.setEstado("solicitado");
        Devolucion saved = devolucionRepo.save(d);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "status", saved.getEstado()));
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o).trim(); }
}
