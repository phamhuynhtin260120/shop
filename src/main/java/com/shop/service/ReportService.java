package com.shop.service;

import com.shop.dto.DashboardStats;
import com.shop.dto.DashboardStats.DailyRevenue;
import com.shop.dto.DashboardStats.TopProduct;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.entity.User;
import com.shop.repository.OrderRepository;
import com.shop.repository.ProductRepository;
import com.shop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Thống kê doanh thu cho trang quản trị.
 * Doanh thu chỉ tính các đơn đã xác nhận/đang giao/đã giao (không tính đơn chờ thanh toán hoặc đã hủy).
 * Dữ liệu đồ án nhỏ nên tính trực tiếp trong Java; khi dữ liệu lớn nên chuyển sang truy vấn GROUP BY.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    static final Set<String> REVENUE_STATUSES = Set.of("PAID", "SHIPPING", "DELIVERED");
    private static final List<String> STATUS_ORDER = List.of("PENDING", "PAID", "SHIPPING", "DELIVERED", "CANCELLED");

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReportService(OrderRepository orderRepository, UserRepository userRepository,
                         ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public DashboardStats dashboard() {
        List<Order> orders = orderRepository.findAll();
        List<Order> counted = orders.stream().filter(o -> REVENUE_STATUSES.contains(o.getStatus())).toList();

        BigDecimal revenue = counted.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = counted.isEmpty() ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(counted.size()), 0, RoundingMode.HALF_UP);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        STATUS_ORDER.forEach(s -> byStatus.put(s, 0L));
        orders.forEach(o -> byStatus.merge(o.getStatus(), 1L, Long::sum));

        return new DashboardStats(
                revenue,
                orders.size(),
                counted.size(),
                average,
                userRepository.countByRole(User.ROLE_CUSTOMER),
                productRepository.countByActiveTrue(),
                byStatus,
                last7Days(counted),
                topProducts(counted));
    }

    private List<DailyRevenue> last7Days(List<Order> counted) {
        LocalDate today = LocalDate.now();
        Map<LocalDate, BigDecimal> byDay = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            byDay.put(today.minusDays(i), BigDecimal.ZERO);
        }
        counted.forEach(o -> byDay.computeIfPresent(o.getCreatedAt().toLocalDate(), (d, v) -> v.add(o.getTotal())));

        BigDecimal max = byDay.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        List<DailyRevenue> result = new ArrayList<>();
        byDay.forEach((day, amount) -> {
            int percent = max.signum() == 0 ? 0
                    : amount.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue();
            result.add(new DailyRevenue(day.format(fmt), amount, percent));
        });
        return result;
    }

    private List<TopProduct> topProducts(List<Order> counted) {
        Map<Long, TopProduct> agg = new HashMap<>();
        for (Order o : counted) {
            for (OrderItem i : o.getItems()) {
                Long id = i.getProduct().getId();
                TopProduct prev = agg.get(id);
                int qty = (prev == null ? 0 : prev.quantity()) + i.getQuantity();
                BigDecimal rev = (prev == null ? BigDecimal.ZERO : prev.revenue()).add(i.getLineTotal());
                agg.put(id, new TopProduct(id, i.getProductName(), qty, rev));
            }
        }
        return agg.values().stream()
                .sorted(Comparator.comparingInt(TopProduct::quantity).reversed())
                .limit(5)
                .toList();
    }
}
