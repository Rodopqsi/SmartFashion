package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.report.SalesReportService;
import com.smarthfashion.admin.report.dto.SalesSummaryDTO;
import com.smarthfashion.admin.report.dto.TimeSeriesPointDTO;
import com.smarthfashion.admin.report.dto.TopProductDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
public class SalesReportController {
    private final SalesReportService service;

    public SalesReportController(SalesReportService service) {
        this.service = service;
    }

    @GetMapping("/sales")
    public String salesReportPage(Model model) {
        model.addAttribute("title", "Reporte de Ventas");
        // Prefetch summaries for fast initial render
        model.addAttribute("summaryToday", service.todaySummary());
        model.addAttribute("summaryMonth", service.monthSummary());
        model.addAttribute("summaryYear", service.yearSummary());
        return "reports/sales";
    }

    // JSON endpoints
    @GetMapping("/api/sales/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("today", service.todaySummary());
        payload.put("month", service.monthSummary());
        payload.put("year", service.yearSummary());
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/api/sales/daily30")
    public ResponseEntity<List<TimeSeriesPointDTO>> daily30() {
        return ResponseEntity.ok(service.last30Days());
    }

    @GetMapping("/api/sales/monthly12")
    public ResponseEntity<List<TimeSeriesPointDTO>> monthly12() {
        return ResponseEntity.ok(service.last12Months());
    }

    @GetMapping("/api/sales/top-products")
    public ResponseEntity<List<TopProductDTO>> topProducts() {
        return ResponseEntity.ok(service.topProductsLast30Days());
    }
}
