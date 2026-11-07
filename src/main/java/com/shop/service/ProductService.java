package com.shop.service;

import com.shop.dto.ProductForm;
import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.exception.ProductNotFoundException;
import com.shop.exception.BusinessException;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tầng nghiệp vụ cho sản phẩm.
 * Controller KHÔNG gọi thẳng Repository mà luôn đi qua Service (kiến trúc 3 tầng).
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    public static final int PAGE_SIZE = 8;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // DIP: Spring tự "tiêm" các Repository vào qua constructor
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getNewArrivals() {
        return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    public List<Product> getBestSellers() {
        return productRepository.findTop4ByActiveTrueAndBestSellerTrueOrderByIdAsc();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<Product> getRelated(Product product) {
        return productRepository.findTop4ByActiveTrueAndCategoryIdAndIdNot(
                product.getCategory().getId(), product.getId());
    }

    /**
     * Danh sách có lọc + phân trang.
     * Ưu tiên: từ khóa tìm kiếm > danh mục > bán chạy > tất cả.
     */
    public Page<Product> browse(String category, String keyword, boolean bestSeller, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        if (keyword != null && !keyword.isBlank()) {
            return productRepository.findByActiveTrueAndNameContainingIgnoreCase(keyword.trim(), pageable);
        }
        if (category != null && !category.isBlank()) {
            return productRepository.findByActiveTrueAndCategorySlug(category, pageable);
        }
        if (bestSeller) {
            return productRepository.findByActiveTrueAndBestSellerTrue(pageable);
        }
        return productRepository.findByActiveTrue(pageable);
    }

    // ---------- Quản trị ----------

    /** Tất cả sản phẩm, kể cả sản phẩm đang ẩn. */
    public List<Product> findAllForAdmin() {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Product getForAdmin(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public Product save(ProductForm f) {
        Category category = categoryRepository.findById(f.getCategoryId())
                .orElseThrow(() -> new BusinessException("Danh mục không tồn tại"));
        Product p = f.getId() == null
                ? new Product(f.getName().trim(), f.getDescription(), f.getPrice(), f.getStock(), category, f.isBestSeller())
                : getForAdmin(f.getId());
        p.setName(f.getName().trim());
        p.setDescription(f.getDescription());
        p.setPrice(f.getPrice());
        p.setStock(f.getStock());
        p.setCategory(category);
        p.setBestSeller(f.isBestSeller());
        p.setActive(f.isActive());
        p.setImageUrl(f.getImageUrl() == null || f.getImageUrl().isBlank() ? null : f.getImageUrl().trim());
        return productRepository.save(p);
    }

    /** Ẩn/hiện sản phẩm (không xóa hẳn vì đơn hàng cũ vẫn tham chiếu tới sản phẩm). */
    @Transactional
    public Product toggleActive(Long id) {
        Product p = getForAdmin(id);
        p.setActive(!p.isActive());
        return p;
    }
}
