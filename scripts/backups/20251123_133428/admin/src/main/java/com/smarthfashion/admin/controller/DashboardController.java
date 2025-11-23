package com.smarthfashion.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashboardController {

    @GetMapping({"/", "/admin"})
    public String dashboard(Model model) {
        // Redirige siempre al listado de productos como home del panel
        return "redirect:/admin/products";
    }

    // Endpoint de diagnóstico: requiere rol ADMIN y evita plantillas
    @GetMapping("/admin/plain")
    @ResponseBody
    public String adminPlain() {
        return "Admin OK";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // optionally add a custom login template later
    }
}
