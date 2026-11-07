package com.shop.config;

import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.entity.Voucher;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import com.shop.repository.UserRepository;
import com.shop.repository.VoucherRepository;
import com.shop.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tạo dữ liệu mẫu khi database còn trống (chạy 1 lần lúc khởi động).
 * Muốn làm lại từ đầu: docker compose down -v  rồi chạy lại ứng dụng.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    /** Tài khoản mẫu để chạy thử (ghi trong báo cáo, mục 4.1). */
    public static final String DEMO_EMAIL = "khach@patternstore.vn";
    public static final String DEMO_PASSWORD = "123456";
    public static final String ADMIN_EMAIL = "admin@patternstore.vn";
    public static final String ADMIN_PASSWORD = "admin123";

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final AuthService authService;

    public DataSeeder(CategoryRepository categoryRepository, ProductRepository productRepository,
                      UserRepository userRepository, VoucherRepository voucherRepository,
                      AuthService authService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.voucherRepository = voucherRepository;
        this.authService = authService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDemoUser();
        seedCatalog();
        seedVouchers();
    }

    private void seedVouchers() {
        if (voucherRepository.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        voucherRepository.save(new Voucher("GIAM10", Voucher.PERCENT, new BigDecimal("10"),
                new BigDecimal("300000"), today.plusMonths(2), 100));
        voucherRepository.save(new Voucher("GIAM50K", Voucher.FIXED, new BigDecimal("50000"),
                new BigDecimal("400000"), today.plusMonths(2), 100));
        voucherRepository.save(new Voucher("HETHAN", Voucher.PERCENT, new BigDecimal("20"),
                BigDecimal.ZERO, today.minusDays(1), 100));
    }

    private void seedDemoUser() {
        User customer = userRepository.findByEmail(DEMO_EMAIL)
                .orElseGet(() -> new User(DEMO_EMAIL, "Nguyễn Văn Khách", "0901234567",
                        "12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP. Hồ Chí Minh"));
        if (customer.getPasswordHash() == null) {            // tài khoản tạo từ phiên bản trước chưa có mật khẩu
            customer.setPasswordHash(authService.hash(DEMO_PASSWORD));
        }
        userRepository.save(customer);

        if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
            User admin = new User(ADMIN_EMAIL, "Quản trị viên", "0909999999", "Pattern Store");
            admin.setRole(User.ROLE_ADMIN);
            admin.setPasswordHash(authService.hash(ADMIN_PASSWORD));
            userRepository.save(admin);
        }
    }

    private void seedCatalog() {
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
