package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.ShippingCompany;
import com.smarthfashion.admin.repository.ShippingCompanyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/shipping/companies")
public class ShippingCompanyController {
    private final ShippingCompanyRepository repo;

    public ShippingCompanyController(ShippingCompanyRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("title", "Empresas de Envío");
        model.addAttribute("companies", repo.findAll());
        model.addAttribute("company", new ShippingCompany());
        return "shipping/companies";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("company") ShippingCompany company,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Empresas de Envío");
            model.addAttribute("companies", repo.findAll());
            return "shipping/companies";
        }
        repo.save(company);
        return "redirect:/admin/shipping/companies";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/admin/shipping/companies";
    }
}
