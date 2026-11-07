package com.shop.controller.admin;

import com.shop.pattern.behavioral.state.OrderStateFactory;
import com.shop.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.Map;

/** Trang tổng quan: doanh thu, đơn theo trạng thái, doanh thu 7 ngày, sản phẩm bán chạy. */
@Controller
public class AdminDashboardController {

    private final ReportService reportService;

    public AdminDashboardController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        var stats = reportService.dashboard();
        Map<String, String> labels = new LinkedHashMap<>();
        stats.ordersByStatus().keySet().forEach(s -> labels.put(s, OrderStateFactory.from(s).label()));
        model.addAttribute("stats", stats);
        model.addAttribute("statusLabels", labels);
        return "admin/dashboard";
    }
}
