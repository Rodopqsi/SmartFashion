package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.Devolucion;
import com.smarthfashion.admin.domain.Reclamacion;
import com.smarthfashion.admin.repository.DevolucionRepository;
import com.smarthfashion.admin.repository.ReclamacionRepository;
import com.smarthfashion.admin.service.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/admin/support")
public class SupportController {
    private final ReclamacionRepository reclamoRepo;
    private final DevolucionRepository devolucionRepo;
    private final EmailService emailService;

    public SupportController(ReclamacionRepository reclamoRepo, DevolucionRepository devolucionRepo, EmailService emailService) {
        this.reclamoRepo = reclamoRepo;
        this.devolucionRepo = devolucionRepo;
        this.emailService = emailService;
    }

    @GetMapping("/claims")
    public String listClaims(Model model,
                             @RequestParam(value = "estado", required = false) String estado,
                             @RequestParam(value = "q", required = false) String q,
                             @RequestParam(value = "desde", required = false) String desde,
                             @RequestParam(value = "hasta", required = false) String hasta) {
        Instant dDesde = parseStart(desde);
        Instant dHasta = parseEnd(hasta);
        List<Reclamacion> claims = reclamoRepo.search(emptyToNull(estado), emptyToNull(q), dDesde, dHasta);
        model.addAttribute("title", "Reclamaciones");
        model.addAttribute("claims", claims);
        model.addAttribute("estado", estado);
        model.addAttribute("q", q);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "support/claims";
    }

    @GetMapping("/claims/{id}")
    public String claimDetail(@PathVariable Long id, Model model) {
        Reclamacion c = reclamoRepo.findById(id).orElse(null);
        model.addAttribute("title", "Detalle Reclamación");
        model.addAttribute("claim", c);
        return "support/claim-detail";
    }

    @PostMapping("/claims/{id}")
    public String updateClaim(@PathVariable Long id,
                              @RequestParam(value = "estado", required = false) String estado,
                              @RequestParam(value = "respuesta", required = false) String respuesta) {
        Reclamacion c = reclamoRepo.findById(id).orElse(null);
        if (c != null) {
            boolean changed = false;
            if (StringUtils.hasText(estado) && !estado.equals(c.getEstado())) { c.setEstado(estado); changed = true; }
            if (respuesta != null && !respuesta.equals(c.getRespuesta())) { c.setRespuesta(respuesta); changed = true; }
            reclamoRepo.save(c);
            if (changed) emailService.notifyClaimStatus(c);
        }
        return "redirect:/admin/support/claims/" + id;
    }

    @GetMapping("/returns")
    public String listReturns(Model model,
                              @RequestParam(value = "estado", required = false) String estado,
                              @RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "desde", required = false) String desde,
                              @RequestParam(value = "hasta", required = false) String hasta) {
        Instant dDesde = parseStart(desde);
        Instant dHasta = parseEnd(hasta);
        List<Devolucion> returnsList = devolucionRepo.search(emptyToNull(estado), emptyToNull(q), dDesde, dHasta);
        model.addAttribute("title", "Devoluciones");
        model.addAttribute("returns", returnsList);
        model.addAttribute("estado", estado);
        model.addAttribute("q", q);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "support/returns";
    }

    @GetMapping("/returns/{id}")
    public String returnDetail(@PathVariable Long id, Model model) {
        Devolucion d = devolucionRepo.findById(id).orElse(null);
        model.addAttribute("title", "Detalle Devolución");
        model.addAttribute("ret", d);
        return "support/return-detail";
    }

    @PostMapping("/returns/{id}")
    public String updateReturn(@PathVariable Long id,
                               @RequestParam(value = "estado", required = false) String estado) {
        Devolucion d = devolucionRepo.findById(id).orElse(null);
        if (d != null && StringUtils.hasText(estado)) {
            boolean changed = !estado.equals(d.getEstado());
            d.setEstado(estado);
            devolucionRepo.save(d);
            if (changed) emailService.notifyReturnStatus(d);
        }
        return "redirect:/admin/support/returns/" + id;
    }

    private static Instant parseStart(String s) {
        try {
            if (!StringUtils.hasText(s)) return null;
            LocalDate ld = LocalDate.parse(s);
            return ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) { return null; }
    }
    private static Instant parseEnd(String s) {
        try {
            if (!StringUtils.hasText(s)) return null;
            LocalDate ld = LocalDate.parse(s).plusDays(1);
            return ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) { return null; }
    }
    private static String emptyToNull(String v){ return StringUtils.hasText(v) ? v : null; }
}
