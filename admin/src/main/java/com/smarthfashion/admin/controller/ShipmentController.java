package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.*;
import com.smarthfashion.admin.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/shipments")
public class ShipmentController {
    private final ShipmentRepository shipmentRepo;
    private final ShipmentEventRepository eventRepo;
    private final DistributionCenterRepository centerRepo;
    private final ShippingRuleRepository ruleRepo;
    private final ShippingCompanyRepository companyRepo;
    private final OrdersRepository ordersRepo;

    public ShipmentController(ShipmentRepository shipmentRepo,
                              ShipmentEventRepository eventRepo,
                              DistributionCenterRepository centerRepo,
                              ShippingRuleRepository ruleRepo,
                              ShippingCompanyRepository companyRepo,
                              OrdersRepository ordersRepo) {
        this.shipmentRepo = shipmentRepo;
        this.eventRepo = eventRepo;
        this.centerRepo = centerRepo;
        this.ruleRepo = ruleRepo;
        this.companyRepo = companyRepo;
        this.ordersRepo = ordersRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("title", "Envíos");
        model.addAttribute("shipments", shipmentRepo.findAll());
        return "shipping/shipments";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(value = "orderNumber", required = false) String orderNumber,
                             Model model) {
        model.addAttribute("title", "Nuevo Envío");
        model.addAttribute("shipment", new Shipment());
        model.addAttribute("centers", centerRepo.findAll());
        if (orderNumber != null && !orderNumber.isBlank()) {
            ordersRepo.findByOrderNumber(orderNumber).ifPresent(o -> model.addAttribute("foundOrder", o));
            model.addAttribute("searchedOrderNumber", orderNumber);
        }
        return "shipping/shipment_form";
    }

    @PostMapping
    public String create(@ModelAttribute Shipment shipment) {
        // Auto-asignación de empresa según reglas (origen = región del centro, destino = regiónDestino)
        DistributionCenter center = centerRepo.findById(shipment.getCentroDistribucion().getId()).orElse(null);
        if (center != null) {
            List<ShippingRule> rules = ruleRepo.findByOrigenRegionIgnoreCaseAndDestinoRegionIgnoreCaseOrderByPrioridadAsc(center.getRegion(), shipment.getRegionDestino());
            if (!rules.isEmpty()) {
                ShippingRule r = rules.get(0);
                shipment.setEmpresaEnvio(r.getEmpresaEnvio());
                shipment.setCostoEnvio(r.getCosto());
                shipment.setStatus(ShipmentStatus.ASIGNADO);
            } else {
                // Fallback: primera empresa activa por cobertura o cualquiera
                List<ShippingCompany> candidates = companyRepo.findByCoberturaIgnoreCase(shipment.getRegionDestino());
                if (candidates.isEmpty()) candidates = companyRepo.findByActivoTrueOrderByNombreAsc();
                if (!candidates.isEmpty()) {
                    shipment.setEmpresaEnvio(candidates.get(0));
                    shipment.setCostoEnvio(null);
                    shipment.setStatus(ShipmentStatus.ASIGNADO);
                }
            }
        }
        Shipment saved = shipmentRepo.save(shipment);
        // Primer evento
        ShipmentEvent ev = new ShipmentEvent();
        ev.setEnvio(saved);
        ev.setStatus(saved.getStatus());
    ev.setNota("Envío creado" + (saved.getEmpresaEnvio() != null ? " y asignado a " + saved.getEmpresaEnvio().getNombre() : "")
        + (saved.getCostoEnvio() != null ? ", costo S/ " + saved.getCostoEnvio() : ""));
        eventRepo.save(ev);
        return "redirect:/admin/shipments/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Shipment s = shipmentRepo.findById(id).orElse(null);
        if (s == null) return "redirect:/admin/shipments";
        model.addAttribute("title", "Envío #" + id);
        model.addAttribute("shipment", s);
        model.addAttribute("events", eventRepo.findByEnvioOrderByFechaAsc(s));
        model.addAttribute("statuses", ShipmentStatus.values());
        model.addAttribute("companies", companyRepo.findAll());
        return "shipping/shipment_detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id, @RequestParam("status") ShipmentStatus status,
                               @RequestParam(value = "nota", required = false) String nota) {
        Shipment s = shipmentRepo.findById(id).orElse(null);
        if (s == null) return "redirect:/admin/shipments";
        s.setStatus(status);
        shipmentRepo.save(s);
        ShipmentEvent ev = new ShipmentEvent();
        ev.setEnvio(s);
        ev.setStatus(status);
        ev.setNota(nota == null ? "Estado actualizado" : nota);
        eventRepo.save(ev);
        return "redirect:/admin/shipments/" + id;
    }

    @PostMapping("/{id}/assign")
    public String reassign(@PathVariable("id") Long id, @RequestParam("companyId") Long companyId,
                           @RequestParam(value = "nota", required = false) String nota) {
        Shipment s = shipmentRepo.findById(id).orElse(null);
        ShippingCompany c = companyRepo.findById(companyId).orElse(null);
        if (s != null && c != null) {
            s.setEmpresaEnvio(c);
            if (s.getStatus() == ShipmentStatus.CREADO) s.setStatus(ShipmentStatus.ASIGNADO);
            shipmentRepo.save(s);
            ShipmentEvent ev = new ShipmentEvent();
            ev.setEnvio(s);
            ev.setStatus(s.getStatus());
            ev.setNota(nota == null ? ("Reasignado a " + c.getNombre()) : nota);
            eventRepo.save(ev);
        }
        return "redirect:/admin/shipments/" + id;
    }
}
