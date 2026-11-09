package com.shop.storage;

import com.shop.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Strategy: lưu ảnh sản phẩm qua ImageStorage")
class ImageServiceTest {

    /** Một chiến lược lưu ảnh trong bộ nhớ, chỉ dùng để test: ImageService không cần biết khác biệt. */
    static class InMemoryStorage implements ImageStorage {
        final Map<String, StoredImage> files = new HashMap<>();
        public void store(String name, String type, byte[] data) { files.put(name, new StoredImage(data, type)); }
        public Optional<StoredImage> load(String name) { return Optional.ofNullable(files.get(name)); }
    }

    private final InMemoryStorage storage = new InMemoryStorage();
    private final ImageService service = new ImageService(storage);

    @Test
    @DisplayName("Tải ảnh PNG: lưu qua strategy, trả về đường dẫn /media/<uuid>.png và đọc lại được")
    void uploadPng() {
        String url = service.upload(new MockMultipartFile("imageFile", "ao.png", "image/png", new byte[]{1, 2, 3}));

        assertTrue(url.matches("^/media/[a-f0-9-]{36}\\.png$"));
        String name = url.substring(ImageService.URL_PREFIX.length());
        assertArrayEquals(new byte[]{1, 2, 3}, service.load(name).orElseThrow().data());
    }

    @Test
    @DisplayName("Từ chối file không phải ảnh")
    void rejectNonImage() {
        assertThrows(BusinessException.class, () ->
                service.upload(new MockMultipartFile("imageFile", "a.pdf", "application/pdf", new byte[]{1})));
        assertTrue(storage.files.isEmpty());
    }

    @Test
    @DisplayName("Không cho đọc file ngoài thư mục (path traversal)")
    void rejectUnsafeName() {
        assertTrue(service.load("../../etc/passwd").isEmpty());
    }
}
