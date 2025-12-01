package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.Collection;
import com.smarthfashion.admin.domain.Product;
import com.smarthfashion.admin.repository.CollectionRepository;
import com.smarthfashion.admin.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/collections")
public class CollectionController {
    private final CollectionRepository collectionRepo;
    private final ProductRepository productRepo;

    public CollectionController(CollectionRepository collectionRepo, ProductRepository productRepo) {
        this.collectionRepo = collectionRepo;
        this.productRepo = productRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("title", "Colecciones");
        model.addAttribute("items", collectionRepo.findAllByOrderByOrdenAscIdDesc());
        return "collections/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("title", "Nueva Colección");
        model.addAttribute("item", new Collection());
        model.addAttribute("productIds", "");
        return "collections/form";
    }

    @PostMapping
    public String create(@ModelAttribute Collection item, @RequestParam(required = false, name = "productIds") String productIds) {
        normalize(item);
        attachProducts(item, productIds);
        collectionRepo.save(item);
        return "redirect:/admin/collections";
    }

    @GetMapping("/{id:\\d+}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Collection item = collectionRepo.findById(id).orElse(null);
        if (item == null) return "redirect:/admin/collections";
        model.addAttribute("title", "Editar Colección");
        model.addAttribute("item", item);
        String ids = item.getProductos().stream().map(p -> String.valueOf(p.getId())).collect(Collectors.joining(","));
        model.addAttribute("productIds", ids);
        return "collections/form";
    }

    @PostMapping("/{id:\\d+}")
    public String update(@PathVariable("id") Long id, @ModelAttribute Collection form,
                         @RequestParam(required = false, name = "productIds") String productIds) {
        Collection item = collectionRepo.findById(id).orElse(null);
        if (item == null) return "redirect:/admin/collections";
        item.setNombre(form.getNombre());
        item.setSlug(form.getSlug());
        item.setDescripcion(form.getDescripcion());
        item.setImageUrl(form.getImageUrl());
        item.setActivo(form.isActivo());
        item.setOrden(form.getOrden());
        attachProducts(item, productIds);
        collectionRepo.save(item);
        return "redirect:/admin/collections/" + id;
    }

    @PostMapping("/{id:\\d+}/delete")
    public String delete(@PathVariable("id") Long id) {
        collectionRepo.deleteById(id);
        return "redirect:/admin/collections";
    }

    private void normalize(Collection c) {
        if (!StringUtils.hasText(c.getSlug()) && StringUtils.hasText(c.getNombre())) {
            String slug = c.getNombre().toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9áéíóúñ\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replace('ñ','n');
            c.setSlug(slug);
        }
    }

    private void attachProducts(Collection c, String productIds) {
        Set<Product> set = new HashSet<>();
        if (StringUtils.hasText(productIds)) {
            List<Long> ids = Arrays.stream(productIds.split(","))
                    .map(String::trim)
                    .filter(s -> s.matches("\\d+"))
                    .map(Long::valueOf)
                    .distinct()
                    .limit(500)
                    .toList();
            if (!ids.isEmpty()) {
                set.addAll(productRepo.findAllById(ids));
            }
        }
        c.setProductos(set);
    }

    @GetMapping("/search-products")
    @ResponseBody
    public List<Map<String, Object>> searchProducts(@RequestParam(name = "q", required = false) String q) {
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        List<Product> results = (query == null) ? productRepo.findAll() : productRepo.search(query, null, null, null);
        return results.stream()
                .limit(30)
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("nombre", p.getNombre());
                    m.put("precio", p.getPrecio());
                    return m;
                })
                .toList();
    }
}
