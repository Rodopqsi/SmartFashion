package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.Color;
import com.smarthfashion.admin.repository.ColorRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/colors")
public class ColorController {
    private final ColorRepository colorRepository;

    public ColorController(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @GetMapping
    public String list(Model model){
        model.addAttribute("colors", colorRepository.findAll());
        model.addAttribute("color", new Color());
        return "colors/list";
    }

    @PostMapping
    public String create(@ModelAttribute Color color){
        colorRepository.save(color);
        return "redirect:/admin/colors";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id){
        colorRepository.deleteById(id);
        return "redirect:/admin/colors";
    }
}
