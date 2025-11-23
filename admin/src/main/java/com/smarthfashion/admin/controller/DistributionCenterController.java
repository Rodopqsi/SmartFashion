package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.DistributionCenter;
import com.smarthfashion.admin.repository.DistributionCenterRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/shipping/centers")
public class DistributionCenterController {
    private final DistributionCenterRepository repo;

    public DistributionCenterController(DistributionCenterRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("title", "Centros de Distribución");
        model.addAttribute("centers", repo.findAll());
        model.addAttribute("center", new DistributionCenter());
        return "shipping/centers";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("center") DistributionCenter center,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Centros de Distribución");
            model.addAttribute("centers", repo.findAll());
            return "shipping/centers";
        }
        repo.save(center);
        return "redirect:/admin/shipping/centers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/admin/shipping/centers";
    }
}
