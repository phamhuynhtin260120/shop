package com.shop.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/** Số liệu cho trang thống kê của quản trị viên. */
public record DashboardStats(
        BigDecimal revenue,
        long totalOrders,
        long completedOrders,
        BigDecimal averageOrderValue,
        long customers,
        long activeProducts,
        Map<String, Long> ordersByStatus,
        List<DailyRevenue> last7Days,
        List<TopProduct> topProducts) {

    /** Doanh thu một ngày; percent = chiều cao cột so với ngày cao nhất (0–100). */
    public record DailyRevenue(String label, BigDecimal amount, int percent) {
        /** Số tiền tính theo nghìn đồng, để ghi trên đầu cột biểu đồ. */
        public long thousands() {
            return amount.divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP).longValue();
        }
    }

    public record TopProduct(Long productId, String name, int quantity, BigDecimal revenue) { }
}
