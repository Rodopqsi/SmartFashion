package com.smarthfashion.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashboardController {

    @GetMapping({"/", "/admin"})
    public String dashboard(Model model) {
        
        return "redirect:/admin/products";
    }

    
    @GetMapping("/admin/plain")
    @ResponseBody
    public String adminPlain() {
        return "Admin OK";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; 
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        // Canonicalizar: redirigir a /login para evitar confusiones de método (POST vs GET)
        return "redirect:/login";
    }
}
