package com.shop.service;

import com.shop.entity.Product;
import com.shop.exception.ProductNotFoundException;
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

    // DIP: Spring tự "tiêm" ProductRepository vào qua constructor
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
}
