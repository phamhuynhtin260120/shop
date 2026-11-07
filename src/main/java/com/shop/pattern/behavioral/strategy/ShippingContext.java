package com.shop.pattern.behavioral.strategy;

import com.shop.entity.Cart;
import com.shop.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Mẫu STRATEGY – vai trò Context.
 * Spring tự gom mọi bean ShippingStrategy vào Map với key là tên bean
 * ("STANDARD", "EXPRESS", "FREE"). Context chọn chiến lược lúc chạy.
 * Thêm hình thức giao mới = thêm 1 lớp @Component, không sửa lớp này (OCP).
 */
@Service
public class ShippingContext {

    private final Map<String, ShippingStrategy> strategies;

    public ShippingContext(Map<String, ShippingStrategy> strategies) {
        this.strategies = strategies;
    }

    /** Chọn chiến lược thực tế cho lựa chọn của khách. */
    public ShippingStrategy resolve(String method, Cart cart) {
        if ("STANDARD".equals(method) && cart.getSubtotal().compareTo(FreeShipping.THRESHOLD) >= 0) {
            return strategies.get("FREE");
        }
        ShippingStrategy strategy = strategies.get(method);
        if (strategy == null || "FREE".equals(method)) {
            throw new BusinessException("Phương thức vận chuyển không hợp lệ: " + method);
        }
        return strategy;
    }

    /** Các lựa chọn hiển thị cho khách ở trang thanh toán. */
    public List<ShippingStrategy> options() {
        // FREE không phải lựa chọn của khách mà được áp tự động
        return strategies.values().stream()
                .filter(s -> !"FREE".equals(s.code()))
                .sorted(java.util.Comparator.comparing(ShippingStrategy::estimatedDays).reversed())
                .toList();
    }
}
