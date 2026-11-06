package com.shop.repository;

import com.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA tự sinh câu SQL từ TÊN phương thức.
 * Ví dụ findTop8ByActiveTrueOrderByCreatedAtDesc ->
 *   SELECT * FROM products WHERE active = true ORDER BY created_at DESC LIMIT 8
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findTop8ByActiveTrueOrderByCreatedAtDesc();

    List<Product> findTop4ByActiveTrueAndBestSellerTrueOrderByIdAsc();

    List<Product> findTop4ByActiveTrueAndCategoryIdAndIdNot(Long categoryId, Long productId);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByActiveTrueAndBestSellerTrue(Pageable pageable);

    Page<Product> findByActiveTrueAndCategorySlug(String slug, Pageable pageable);

    Page<Product> findByActiveTrueAndNameContainingIgnoreCase(String keyword, Pageable pageable);
}
