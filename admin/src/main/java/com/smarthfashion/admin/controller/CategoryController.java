package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.Category;
import com.smarthfashion.admin.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String list(Model model){
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("category", new Category());
        return "categories/list";
    }

    @PostMapping
    public String create(@ModelAttribute Category category){
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id){
        categoryRepository.deleteById(id);
        return "redirect:/admin/categories";
    }
}
