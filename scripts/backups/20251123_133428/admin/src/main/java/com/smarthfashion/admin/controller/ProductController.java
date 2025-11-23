package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.*;
import com.smarthfashion.admin.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;

    @Value("${app.upload-dir:uploads/}")
    private String uploadDir;

    public ProductController(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             ProductVariantRepository variantRepository,
                             ProductImageRepository imageRepository,
                             ColorRepository colorRepository,
                             SizeRepository sizeRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.colorRepository = colorRepository;
        this.sizeRepository = sizeRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("colors", colorRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
        return "products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("product") Product product,
                         BindingResult bindingResult,
                         @RequestParam(name = "sizeId", required = false) Long sizeId,
                         @RequestParam(name = "colorId", required = false) Long colorId,
                         @RequestParam(name = "stock", defaultValue = "0") Integer stock,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("colors", colorRepository.findAll());
            model.addAttribute("sizes", sizeRepository.findAll());
            return "products/form";
        }

        // Validación simple: requerir talla y color para la primera variante
        if (sizeId == null || colorId == null) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("colors", colorRepository.findAll());
            model.addAttribute("sizes", sizeRepository.findAll());
            model.addAttribute("formError", "Selecciona talla y color para crear la primera variante");
            return "products/form";
        }

        // 1) Guardar producto base
        Product saved = productRepository.save(product);

        // 2) Crear variante inicial con stock, talla y color
        ProductVariant v = new ProductVariant();
        v.setProduct(saved);
        v.setSize(sizeRepository.findById(sizeId).orElse(null));
        v.setColor(colorRepository.findById(colorId).orElse(null));
        v.setStock(stock == null ? 0 : stock);
        variantRepository.save(v);

        // 3) Crear al menos una imagen de variante usando el imagePreview como mínima
        if (saved.getImagePreview() != null && !saved.getImagePreview().isBlank()) {
            ProductImage im = new ProductImage();
            im.setProduct(saved);
            im.setSize(v.getSize());
            im.setColor(v.getColor());
            im.setUrl(saved.getImagePreview());
            imageRepository.save(im);
        }

        return "redirect:/admin/products/" + saved.getId() + "/edit";
    }

    // Edit page: variants + images by variant
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model){
        java.util.Optional<Product> opt = productRepository.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/admin/products?err=Producto%20no%20encontrado";
        }
        Product p = opt.get();
        model.addAttribute("product", p);
        // Fetch related data with repository queries to avoid NPEs on orphan rows
        model.addAttribute("variants", variantRepository.findByProduct_Id(id));
        model.addAttribute("images", imageRepository.findByProduct_Id(id));
        model.addAttribute("colors", colorRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
        return "products/edit";
    }

    // Create variant
    @PostMapping("/{id}/variants")
    public String addVariant(@PathVariable("id") Long id,
                             @RequestParam(name = "sizeId", required = false) Long sizeId,
                             @RequestParam(name = "colorId", required = false) Long colorId,
                             @RequestParam(name = "stock", defaultValue = "0") Integer stock){
        if (sizeId == null || colorId == null) {
            return "redirect:/admin/products/"+id+"/edit?err=Selecciona%20talla%20y%20color%20para%20crear%20una%20variante";
        }
        Product p = productRepository.findById(id).orElseThrow();
        ProductVariant v = new ProductVariant();
        v.setProduct(p);
        v.setSize(sizeRepository.findById(sizeId).orElse(null));
        v.setColor(colorRepository.findById(colorId).orElse(null));
        v.setStock(stock == null ? 0 : stock);
        variantRepository.save(v);
        return "redirect:/admin/products/"+id+"/edit";
    }

    // Delete variant
    @PostMapping("/{id}/variants/{variantId}/delete")
    public String deleteVariant(@PathVariable("id") Long id, @PathVariable("variantId") Long variantId){
        variantRepository.deleteById(variantId);
        return "redirect:/admin/products/"+id+"/edit";
    }

    // Add images by variant or by color/general (unified: URLs o archivos locales)
    @PostMapping(path = "/{id}/images")
    public String addImages(@PathVariable("id") Long id,
                            @RequestParam(name = "sizeId", required = false) Long sizeId,
                            @RequestParam(name = "colorId", required = false) Long colorId,
                            @RequestParam(name = "urls", required = false) String urls,
                            @RequestParam(name = "files", required = false) MultipartFile[] files){
        Product p = productRepository.findById(id).orElseThrow();
        Size s = (sizeId != null ? sizeRepository.findById(sizeId).orElse(null) : null);
        Color c = (colorId != null ? colorRepository.findById(colorId).orElse(null) : null);
        boolean added = false;

        // 1) URLs (si hay)
        if (urls != null && !urls.isBlank()) {
            for (String u : urls.split("\n")){
                String url = u.trim();
                if (url.isEmpty()) continue;
                ProductImage im = new ProductImage();
                im.setProduct(p); im.setSize(s); im.setColor(c); im.setUrl(url);
                imageRepository.save(im);
                added = true;
            }
        }

        // 2) Archivos locales (si hay)
        if (files != null && files.length > 0) {
            try {
                java.nio.file.Path base = java.nio.file.Paths.get(uploadDir);
                java.nio.file.Files.createDirectories(base);
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) continue;
                    String original = file.getOriginalFilename();
                    String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
                    String filename = java.util.UUID.randomUUID().toString().replace("-", "") + ext;
                    java.nio.file.Path dest = base.resolve(filename);
                    file.transferTo(dest.toFile());

                    ProductImage im = new ProductImage();
                    im.setProduct(p); im.setSize(s); im.setColor(c);
                    im.setUrl("/uploads/" + filename);
                    imageRepository.save(im);
                    added = true;
                }
            } catch (Exception e) {
                return "redirect:/admin/products/"+id+"/edit?err=No%20se%20pudo%20subir%20las%20im%C3%A1genes";
            }
        }

        if (!added) {
            return "redirect:/admin/products/"+id+"/edit?err=Ingrese%20URL(s)%20o%20seleccione%20archivo(s)";
        }
        return "redirect:/admin/products/"+id+"/edit";
    }

    // (endpoint /{id}/images/upload queda obsoleto, mantenemos addImages unificado)
    // Delete image
    @PostMapping("/{id}/images/{imageId}/delete")
    public String deleteImage(@PathVariable("id") Long id, @PathVariable("imageId") Long imageId){
        imageRepository.deleteById(imageId);
        return "redirect:/admin/products/"+id+"/edit";
    }
}
