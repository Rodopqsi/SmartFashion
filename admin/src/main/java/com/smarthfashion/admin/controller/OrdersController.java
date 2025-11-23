package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.OrderStatus;
import com.smarthfashion.admin.domain.Orders;
import com.smarthfashion.admin.repository.OrderItemRepository;
import com.smarthfashion.admin.service.EmailService;
import com.smarthfashion.admin.repository.OrdersRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
@RequestMapping("/admin/orders")
public class OrdersController {
    private final OrdersRepository ordersRepo;
    private final OrderItemRepository itemRepo;
    private final EmailService emailService;

    public OrdersController(OrdersRepository ordersRepo, OrderItemRepository itemRepo, EmailService emailService) {
        this.ordersRepo = ordersRepo;
        this.itemRepo = itemRepo;
        this.emailService = emailService;
    }

    @GetMapping
    public String list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Orders> spec = Specification.where(null);

        if (from != null && to != null) {
            LocalDateTime start = from.atStartOfDay();
            LocalDateTime end = to.atTime(LocalTime.MAX);
            spec = spec.and((root, query, cb) -> cb.between(root.get("createdAt"), start, end));
        }
        if (StringUtils.hasText(q)) {
            String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), like),
                    cb.like(cb.lower(root.get("email")), like)
            ));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Orders> pageData = ordersRepo.findAll(spec, pageable);

        model.addAttribute("title", "Pedidos");
        model.addAttribute("page", pageData);
        model.addAttribute("orders", pageData.getContent());
        model.addAttribute("q", q);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("status", status);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("size", size);
        return "orders/list";
    }

    @GetMapping("/export")
    public void exportCsv(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "status", required = false) OrderStatus status,
            HttpServletResponse response
    ) throws IOException {
        Specification<Orders> spec = Specification.where(null);
        if (from != null && to != null) {
            LocalDateTime start = from.atStartOfDay();
            LocalDateTime end = to.atTime(LocalTime.MAX);
            spec = spec.and((root, query, cb) -> cb.between(root.get("createdAt"), start, end));
        }
        if (StringUtils.hasText(q)) {
            String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), like),
                    cb.like(cb.lower(root.get("email")), like)
            ));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        var data = ordersRepo.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        String filename = "orders-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));

        try (PrintWriter w = response.getWriter()) {
            w.println("id,order_number,email,status,subtotal,igv,total,created_at");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Orders o : data) {
                String st = o.getStatus() != null ? o.getStatus().name() : "";
                String created = o.getCreatedAt() != null ? o.getCreatedAt().format(fmt) : "";
                w.printf(Locale.US, "%d,%s,%s,%s,%.2f,%.2f,%.2f,%s%n",
                        o.getId(), safe(o.getOrderNumber()), safe(o.getEmail()), st,
                        o.getSubtotal() == null ? 0.0 : o.getSubtotal(),
                        o.getIgv() == null ? 0.0 : o.getIgv(),
                        o.getTotal() == null ? 0.0 : o.getTotal(),
                        created);
            }
        }
    }

    private static String safe(String s) { return s == null ? "" : s.replace(",", " "); }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Orders o = ordersRepo.findById(id).orElse(null);
        if (o == null) return "redirect:/admin/orders";
        model.addAttribute("title", "Pedido " + o.getOrderNumber());
        model.addAttribute("order", o);
        model.addAttribute("items", itemRepo.findByOrder_Id(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam("status") OrderStatus status) {
        return ordersRepo.findById(id).map(o -> {
            o.setStatus(status);
            ordersRepo.save(o);
            
            try { emailService.sendOrderStatusEmail(o); } catch (Exception ignore) {}
            return "redirect:/admin/orders/" + id;
        }).orElse("redirect:/admin/orders");
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId){
        return ordersRepo.findById(orderId).map(o -> {
            o.setStatus(OrderStatus.CANCELADO);
            ordersRepo.save(o);
            try { emailService.sendOrderStatusEmail(o); } catch (Exception ignore) {}
            return "redirect:/admin/orders/" + orderId;
        }).orElse("redirect:/admin/orders");
    }

    @PostMapping("/{orderId}/items/{itemId}/delete")
    public String deleteItem(@PathVariable("orderId") Long orderId, @PathVariable("itemId") Long itemId){
        var orderOpt = ordersRepo.findById(orderId);
        if (orderOpt.isEmpty()) return "redirect:/admin/orders";
        var order = orderOpt.get();
        itemRepo.deleteById(itemId);
        
        var items = itemRepo.findByOrder_Id(orderId);
        java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;
        for (var it : items){
            if (it.getAmount() != null) subtotal = subtotal.add(it.getAmount());
        }
        var igv = subtotal.multiply(java.math.BigDecimal.valueOf(0.18));
        var total = subtotal.add(igv);
        order.setSubtotal(subtotal);
        order.setIgv(igv);
        order.setTotal(total);
        ordersRepo.save(order);
        return "redirect:/admin/orders/" + orderId;
    }
}
