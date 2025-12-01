package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.report.SalesReportService;
import com.smarthfashion.admin.report.dto.SalesSummaryDTO;
import com.smarthfashion.admin.report.dto.TimeSeriesPointDTO;
import com.smarthfashion.admin.report.dto.TopProductDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        service.logBasicDiagnostics();
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

    @GetMapping("/api/sales/daily")
    public ResponseEntity<List<TimeSeriesPointDTO>> daily(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(service.dailyBetween(from, to));
    }

    @GetMapping("/api/sales/monthly")
    public ResponseEntity<List<TimeSeriesPointDTO>> monthly(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(service.monthlyBetween(from, to));
    }

    @GetMapping("/api/sales/top-products")
    public ResponseEntity<List<TopProductDTO>> topProducts(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(service.topProductsBetween(from, to));
    }

    @GetMapping(value = "/api/sales/export.csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        String csv = service.exportCsv(from, to);
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
