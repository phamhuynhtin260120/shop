package com.shop.storage;

import java.util.Optional;

/**
 * Mẫu STRATEGY (kết hợp DIP) cho việc lưu ảnh sản phẩm.
 * ImageService chỉ biết interface này; cài đặt cụ thể được Spring chọn theo profile:
 *  - LocalImageStorage  (máy cá nhân, profile mặc định): lưu vào thư mục uploads/
 *  - GcsImageStorage    (Google Cloud, profile prod): lưu vào bucket Cloud Storage
 * Container trên Cloud Run bị xóa và tạo lại thường xuyên nên KHÔNG được lưu ảnh vào ổ đĩa của nó.
 */
public interface ImageStorage {

    /** Lưu ảnh với tên đã được kiểm tra an toàn. */
    void store(String name, String contentType, byte[] data);

    /** Đọc ảnh theo tên; rỗng nếu không có. */
    Optional<StoredImage> load(String name);

    record StoredImage(byte[] data, String contentType) { }
}
