package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.ShippingRule;
import com.smarthfashion.admin.repository.ShippingRuleRepository;
import com.smarthfashion.admin.repository.ShippingCompanyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/shipping/rules")
public class ShippingRuleController {
    private final ShippingRuleRepository ruleRepo;
    private final ShippingCompanyRepository companyRepo;

    public ShippingRuleController(ShippingRuleRepository ruleRepo, ShippingCompanyRepository companyRepo) {
        this.ruleRepo = ruleRepo;
        this.companyRepo = companyRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("title", "Reglas de Envío");
        model.addAttribute("rules", ruleRepo.findAll());
        model.addAttribute("rule", new ShippingRule());
        model.addAttribute("companies", companyRepo.findAll());
        return "shipping/rules";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("rule") ShippingRule rule,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Reglas de Envío");
            model.addAttribute("rules", ruleRepo.findAll());
            model.addAttribute("companies", companyRepo.findAll());
            return "shipping/rules";
        }
        ruleRepo.save(rule);
        return "redirect:/admin/shipping/rules";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        ruleRepo.deleteById(id);
        return "redirect:/admin/shipping/rules";
    }
}
