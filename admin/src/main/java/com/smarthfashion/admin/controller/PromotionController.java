package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.*;
import com.smarthfashion.admin.repository.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/promotions")
public class PromotionController {
    private final PromotionRepository promotionRepository;
    private final PromotionApplicationRepository applicationRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public PromotionController(PromotionRepository promotionRepository,
                               PromotionApplicationRepository applicationRepository,
                               ProductRepository productRepository,
                               CategoryRepository categoryRepository) {
        this.promotionRepository = promotionRepository;
        this.applicationRepository = applicationRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String list(Model model){
        model.addAttribute("promos", promotionRepository.findAll());
        return "promotions/list";
    }

    @GetMapping("/new")
    public String form(Model model){
        model.addAttribute("promo", new Promotion());
        return "promotions/form";
    }

    @PostMapping
    public String create(@RequestParam(name = "nombre") String nombre,
                         @RequestParam(name = "codigo", required = false) String codigo,
                         @RequestParam(name = "tipoDescuento") String tipoDescuento,
                         @RequestParam(name = "valor") BigDecimal valor,
                         @RequestParam(name = "fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
                         @RequestParam(name = "fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
                         @RequestParam(name = "activo", defaultValue = "1") Long activo){
        Promotion p = new Promotion();
        p.setNombre(nombre); p.setCodigo(codigo); p.setTipoDescuento(tipoDescuento);
        p.setValor(valor); p.setFechaInicio(fechaInicio); p.setFechaFin(fechaFin); p.setActivo(activo);
        promotionRepository.save(p);
        return "redirect:/admin/promotions";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model){
        Promotion promo = promotionRepository.findById(id).orElseThrow();
        model.addAttribute("promo", promo);
        model.addAttribute("apps", applicationRepository.findByPromocionId(id));
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "promotions/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @RequestParam(name = "nombre") String nombre,
                         @RequestParam(name = "codigo", required = false) String codigo,
                         @RequestParam(name = "tipoDescuento") String tipoDescuento,
                         @RequestParam(name = "valor") BigDecimal valor,
                         @RequestParam(name = "fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
                         @RequestParam(name = "fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
                         @RequestParam(name = "activo", defaultValue = "1") Long activo){
        Promotion p = promotionRepository.findById(id).orElseThrow();
        p.setNombre(nombre); p.setCodigo(codigo); p.setTipoDescuento(tipoDescuento);
        p.setValor(valor); p.setFechaInicio(fechaInicio); p.setFechaFin(fechaFin); p.setActivo(activo);
        promotionRepository.save(p);
        return "redirect:/admin/promotions/"+id+"/edit";
    }

    @PostMapping("/{id}/apply/product")
    public String addProduct(@PathVariable("id") Long id, @RequestParam Long productId){
        Promotion promo = promotionRepository.findById(id).orElseThrow();
        Product prod = productRepository.findById(productId).orElseThrow();
        PromotionApplication a = new PromotionApplication();
        a.setPromocion(promo); a.setProducto(prod); a.setCategoria(null);
        applicationRepository.save(a);
        return "redirect:/admin/promotions/"+id+"/edit";
    }

    @PostMapping("/{id}/apply/category")
    public String addCategory(@PathVariable("id") Long id, @RequestParam Long categoryId){
        Promotion promo = promotionRepository.findById(id).orElseThrow();
        Category cat = categoryRepository.findById(categoryId).orElseThrow();
        PromotionApplication a = new PromotionApplication();
        a.setPromocion(promo); a.setCategoria(cat); a.setProducto(null);
        applicationRepository.save(a);
        return "redirect:/admin/promotions/"+id+"/edit";
    }

    @PostMapping("/{id}/apps/{appId}/delete")
    public String removeApp(@PathVariable("id") Long id, @PathVariable("appId") Long appId){
        applicationRepository.deleteById(appId);
        return "redirect:/admin/promotions/"+id+"/edit";
    }
}
