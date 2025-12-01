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
import java.math.BigDecimal;
import java.util.List;

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
    public String list(Model model,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "categoriaId", required = false) String categoriaIdStr,
                       @RequestParam(value = "precioMin", required = false) String precioMinStr,
                       @RequestParam(value = "precioMax", required = false) String precioMaxStr) {
        String qn = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        java.math.BigDecimal precioMin = null;
        java.math.BigDecimal precioMax = null;
        try { if (precioMinStr != null && !precioMinStr.isBlank()) precioMin = new java.math.BigDecimal(precioMinStr.trim()); } catch (Exception ignored) {}
        try { if (precioMaxStr != null && !precioMaxStr.isBlank()) precioMax = new java.math.BigDecimal(precioMaxStr.trim()); } catch (Exception ignored) {}
        Long categoriaId = null;
        try { if (categoriaIdStr != null && !categoriaIdStr.isBlank()) categoriaId = Long.parseLong(categoriaIdStr.trim()); } catch (Exception ignored) {}

        java.util.List<Product> results;
        if (qn != null || categoriaId != null || precioMin != null || precioMax != null) {
            results = productRepository.search(qn, categoriaId, precioMin, precioMax);
        } else {
            results = productRepository.findAll();
        }
        model.addAttribute("products", results);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("q", q);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("precioMin", precioMinStr);
        model.addAttribute("precioMax", precioMaxStr);
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

        
        if (sizeId == null || colorId == null) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("colors", colorRepository.findAll());
            model.addAttribute("sizes", sizeRepository.findAll());
            model.addAttribute("formError", "Selecciona talla y color para crear la primera variante");
            return "products/form";
        }

        
        Product saved = productRepository.save(product);

        
        ProductVariant v = new ProductVariant();
        v.setProduct(saved);
        v.setSize(sizeRepository.findById(sizeId).orElse(null));
        v.setColor(colorRepository.findById(colorId).orElse(null));
        v.setStock(stock == null ? 0 : stock);
        variantRepository.save(v);

        
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

    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model){
        java.util.Optional<Product> opt = productRepository.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/admin/products?err=Producto%20no%20encontrado";
        }
        Product p = opt.get();
        model.addAttribute("product", p);
        
        model.addAttribute("variants", variantRepository.findByProduct_Id(id));
        model.addAttribute("images", imageRepository.findByProduct_Id(id));
        model.addAttribute("colors", colorRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "products/edit";
    }

    
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

    
    @PostMapping("/{id}/variants/{variantId}/delete")
    public String deleteVariant(@PathVariable("id") Long id, @PathVariable("variantId") Long variantId){
        variantRepository.deleteById(variantId);
        return "redirect:/admin/products/"+id+"/edit";
    }

    
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

    
    
    @PostMapping("/{id}/images/{imageId}/delete")
    public String deleteImage(@PathVariable("id") Long id, @PathVariable("imageId") Long imageId){
        imageRepository.deleteById(imageId);
        return "redirect:/admin/products/"+id+"/edit";
    }

    
    @PostMapping("/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @RequestParam("nombre") String nombre,
                                @RequestParam("descripcion") String descripcion,
                                @RequestParam("precio") BigDecimal precio,
                                @RequestParam("categoriaId") Long categoriaId,
                                @RequestParam(value = "imagePreview", required = false) String imagePreview,
                                Model model) {
        Product p = productRepository.findById(id).orElse(null);
        if (p == null) {
            return "redirect:/admin/products?err=Producto%20no%20encontrado";
        }
        if (nombre == null || nombre.isBlank() || descripcion == null || descripcion.isBlank() || precio == null || categoriaId == null) {
            return "redirect:/admin/products/"+id+"/edit?err=Completa%20todos%20los%20campos";
        }
        p.setNombre(nombre.trim());
        p.setDescripcion(descripcion.trim());
        p.setPrecio(precio);
        p.setCategoria(categoryRepository.findById(categoriaId).orElse(null));
        if (imagePreview != null) p.setImagePreview(imagePreview.trim());
        productRepository.save(p);
        return "redirect:/admin/products/"+id+"/edit";
    }

    
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id) {
        // Delete children first to avoid FK constraint issues
        List<ProductImage> imgs = imageRepository.findByProduct_Id(id);
        if (imgs != null && !imgs.isEmpty()) {
            imageRepository.deleteAll(imgs);
        }
        List<ProductVariant> vars = variantRepository.findByProduct_Id(id);
        if (vars != null && !vars.isEmpty()) {
            variantRepository.deleteAll(vars);
        }
        productRepository.deleteById(id);
        return "redirect:/admin/products?ok=Producto%20eliminado";
    }

    
    @PostMapping("/{id}/variants/{variantId}")
    public String updateVariant(@PathVariable("id") Long id,
                                @PathVariable("variantId") Long variantId,
                                @RequestParam(value = "sizeId", required = false) Long sizeId,
                                @RequestParam(value = "colorId", required = false) Long colorId,
                                @RequestParam(value = "stock", required = false) Integer stock) {
        ProductVariant v = variantRepository.findById(variantId).orElse(null);
        if (v == null) {
            return "redirect:/admin/products/"+id+"/edit?err=Variante%20no%20encontrada";
        }
        if (sizeId != null) {
            v.setSize(sizeRepository.findById(sizeId).orElse(null));
        }
        if (colorId != null) {
            v.setColor(colorRepository.findById(colorId).orElse(null));
        }
        if (stock != null && stock >= 0) {
            v.setStock(stock);
        }
        variantRepository.save(v);
        return "redirect:/admin/products/"+id+"/edit";
    }
}
