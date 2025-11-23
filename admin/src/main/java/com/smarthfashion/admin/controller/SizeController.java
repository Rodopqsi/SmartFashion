package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.Size;
import com.smarthfashion.admin.repository.SizeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/sizes")
public class SizeController {
    private final SizeRepository sizeRepository;

    public SizeController(SizeRepository sizeRepository) {
        this.sizeRepository = sizeRepository;
    }

    @GetMapping
    public String list(Model model){
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("size", new Size());
        return "sizes/list";
    }

    @PostMapping
    public String create(@ModelAttribute Size size){
        sizeRepository.save(size);
        return "redirect:/admin/sizes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id){
        sizeRepository.deleteById(id);
        return "redirect:/admin/sizes";
    }
}
