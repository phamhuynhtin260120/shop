package com.shop.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Nhập sản phẩm thời trang mẫu từ DummyJSON (https://dummyjson.com) – API dữ liệu mẫu miễn phí
 * dành cho đồ án và demo – rồi LƯU THẬT vào PostgreSQL.
 *
 * Chạy 1 lần khi khởi động (sau DataSeeder). Nếu đã nhập rồi hoặc không có mạng thì bỏ qua,
 * ứng dụng vẫn chạy bình thường với dữ liệu sẵn có.
 * Tắt bằng cấu hình: app.import.dummyjson=false
 */
@Component
@Order(2)
public class DummyJsonImporter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DummyJsonImporter.class);
    private static final String BASE_URL = "https://dummyjson.com/products/category/";
    private static final String IMAGE_PREFIX = "https://cdn.dummyjson.com";

    /** Tỷ giá quy đổi giá mẫu (USD) sang VND. */
    static final BigDecimal VND_PER_USD = new BigDecimal("25000");

    /** Danh mục DummyJSON -> danh mục của shop (slug, tên hiển thị). */
    private static final Map<String, String[]> CATEGORY_MAP = new LinkedHashMap<>();
    static {
        CATEGORY_MAP.put("mens-shirts", new String[]{"ao", "Áo"});
        CATEGORY_MAP.put("tops", new String[]{"ao", "Áo"});
        CATEGORY_MAP.put("womens-dresses", new String[]{"vay-dam", "Váy đầm"});
        CATEGORY_MAP.put("mens-shoes", new String[]{"giay", "Giày"});
        CATEGORY_MAP.put("womens-shoes", new String[]{"giay", "Giày"});
        CATEGORY_MAP.put("womens-bags", new String[]{"phu-kien", "Phụ kiện"});
        CATEGORY_MAP.put("sunglasses", new String[]{"phu-kien", "Phụ kiện"});
        CATEGORY_MAP.put("womens-jewellery", new String[]{"phu-kien", "Phụ kiện"});
    }

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final boolean enabled;
    private final RestClient restClient = RestClient.create();

    public DummyJsonImporter(CategoryRepository categoryRepository, ProductRepository productRepository,
                             @Value("${app.import.dummyjson:true}") boolean enabled) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.enabled = enabled;
    }

    // ----- Cấu trúc JSON trả về từ DummyJSON (chỉ lấy các trường cần dùng) -----
    @JsonIgnoreProperties(ignoreUnknown = true)
    record DjResponse(List<DjProduct> products) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DjProduct(String title, String description, Double price, Integer stock,
                     Double rating, String thumbnail, List<String> images) { }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (productRepository.existsByImageUrlStartingWith(IMAGE_PREFIX)) {
            log.info("[IMPORT] Đã có sản phẩm từ DummyJSON, bỏ qua bước nhập");
            return;
        }

        List<Product> imported = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, String[]> e : CATEGORY_MAP.entrySet()) {
            try {
                DjResponse res = restClient.get().uri(BASE_URL + e.getKey() + "?limit=0")
                        .retrieve().body(DjResponse.class);
                if (res == null || res.products() == null) {
                    continue;
                }
                Category category = category(e.getValue()[0], e.getValue()[1]);
                for (DjProduct dj : res.products()) {
                    Product p = toProduct(dj, category);
                    p.setCreatedAt(now.minusHours(imported.size()));
                    imported.add(p);
                }
            } catch (Exception ex) {
                log.warn("[IMPORT] Không tải được danh mục {}: {}", e.getKey(), ex.getMessage());
            }
        }

        if (imported.isEmpty()) {
            log.warn("[IMPORT] Không nhập được sản phẩm nào (có thể do không có mạng). Giữ dữ liệu cũ.");
            return;
        }
        productRepository.saveAll(imported);

        // Ẩn các sản phẩm mẫu cũ chưa có ảnh để cửa hàng trông như thật (không xóa vì đơn cũ còn tham chiếu)
        List<Product> placeholders = productRepository.findByImageUrlIsNullAndActiveTrue();
        placeholders.forEach(p -> p.setActive(false));

        log.info("[IMPORT] Đã nhập {} sản phẩm từ DummyJSON, ẩn {} sản phẩm mẫu không có ảnh",
                imported.size(), placeholders.size());
    }

    private Category category(String slug, String name) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(new Category(name, slug)));
    }

    private Product toProduct(DjProduct dj, Category category) {
        Product p = new Product(dj.title(), dj.description(), toVnd(dj.price()),
                dj.stock() == null ? 20 : Math.max(dj.stock(), 0), category,
                dj.rating() != null && dj.rating() >= 4.5);
        String image = dj.images() != null && !dj.images().isEmpty() ? dj.images().get(0) : dj.thumbnail();
        p.setImageUrl(image);
        return p;
    }

    /** Quy đổi USD -> VND và làm tròn kiểu giá bán lẻ: 9.99 USD -> 249.000đ. */
    static BigDecimal toVnd(Double usd) {
        double value = usd == null ? 10 : usd;
        long vnd = (long) Math.ceil(value * VND_PER_USD.doubleValue() / 10_000) * 10_000 - 1_000;
        return BigDecimal.valueOf(Math.max(vnd, 99_000));
    }
}
