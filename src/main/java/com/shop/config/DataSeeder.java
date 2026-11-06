package com.shop.config;

import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tạo dữ liệu mẫu khi database còn trống (chạy 1 lần lúc khởi động).
 * Muốn làm lại từ đầu: docker compose down -v  rồi chạy lại ứng dụng.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataSeeder(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }
        Category ao = categoryRepository.save(new Category("Áo", "ao"));
        Category quan = categoryRepository.save(new Category("Quần", "quan"));
        Category vay = categoryRepository.save(new Category("Váy đầm", "vay-dam"));
        Category pk = categoryRepository.save(new Category("Phụ kiện", "phu-kien"));

        Object[][] data = {
            // tên, mô tả, giá, tồn kho, danh mục, bán chạy
            {"Áo sơ mi linen tay ngắn form rộng", "Chất linen thoáng mát, phù hợp đi làm và đi chơi.", 329000, 40, ao, true},
            {"Áo thun cotton cổ tròn basic", "Cotton 100%, co giãn nhẹ, dễ phối đồ.", 199000, 80, ao, false},
            {"Áo len cổ lọ dệt kim", "Len dệt kim mềm, giữ ấm tốt cho mùa lạnh.", 389000, 25, ao, false},
            {"Áo khoác blazer dáng suông", "Blazer một hàng khuy, lót trong mỏng nhẹ.", 559000, 15, ao, false},
            {"Quần jeans ống suông wash nhẹ", "Denim dày vừa, lưng cao tôn dáng.", 459000, 30, quan, true},
            {"Quần tây ống đứng lưng cao", "Vải tuyết mưa ít nhăn, phù hợp công sở.", 399000, 35, quan, false},
            {"Quần short kaki túi hộp", "Kaki cotton, túi hộp hai bên tiện dụng.", 259000, 50, quan, false},
            {"Đầm midi hoa nhí cổ vuông", "Voan hai lớp, tay phồng nhẹ, dáng xòe.", 489000, 20, vay, true},
            {"Chân váy xếp ly dáng dài", "Xếp ly cố định, lưng thun sau thoải mái.", 349000, 28, vay, false},
            {"Đầm suông tay phồng", "Đầm suông dễ mặc, che khuyết điểm tốt.", 429000, 18, vay, false},
            {"Túi tote canvas in chữ", "Canvas dày, có ngăn nhỏ bên trong.", 189000, 60, pk, true},
            {"Mũ bucket vải dù", "Vải dù nhẹ, chống nắng, gấp gọn được.", 159000, 45, pk, false},
        };

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < data.length; i++) {
            Object[] d = data[i];
            Product p = new Product((String) d[0], (String) d[1],
                    BigDecimal.valueOf((Integer) d[2]), (Integer) d[3],
                    (Category) d[4], (Boolean) d[5]);
            p.setCreatedAt(now.minusDays(i));   // để mục "Hàng mới" có thứ tự
            productRepository.save(p);
        }
    }
}
