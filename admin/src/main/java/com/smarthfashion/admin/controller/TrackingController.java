package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.Shipment;
import com.smarthfashion.admin.repository.ShipmentRepository;
import com.smarthfashion.admin.repository.ShipmentEventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TrackingController {
    private final ShipmentRepository shipmentRepo;
    private final ShipmentEventRepository eventRepo;

    public TrackingController(ShipmentRepository shipmentRepo, ShipmentEventRepository eventRepo) {
        this.shipmentRepo = shipmentRepo;
        this.eventRepo = eventRepo;
    }

    @GetMapping("/tracking/{orderId}")
    public String tracking(@PathVariable String orderId, Model model) {
        try {
            Shipment s = shipmentRepo.findFirstByOrderIdOrderByCreadoEnDesc(orderId).orElse(null);
            if (s == null) {
                model.addAttribute("notFound", true);
                model.addAttribute("orderId", orderId);
                return "tracking/public_tracking";
            }
            model.addAttribute("shipment", s);
            model.addAttribute("events", eventRepo.findByEnvioOrderByFechaAsc(s));
            return "tracking/public_tracking";
        } catch (Exception ex) {
            model.addAttribute("notFound", true);
            model.addAttribute("orderId", orderId);
            model.addAttribute("error", ex.getMessage());
            return "tracking/public_tracking";
        }
    }
}
