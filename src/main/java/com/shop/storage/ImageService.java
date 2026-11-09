package com.shop.storage;

import com.shop.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Kiểm tra ảnh tải lên (loại file, dung lượng), đặt tên ngẫu nhiên rồi giao cho ImageStorage lưu.
 * Không quan tâm ảnh nằm trên ổ đĩa hay trên Cloud Storage (Context của mẫu Strategy).
 */
@Service
public class ImageService {

    public static final String URL_PREFIX = "/media/";
    static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> EXT = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp", "image/gif", "gif");
    /** Chỉ chấp nhận tên do hệ thống sinh ra: chống truy cập file ngoài thư mục (path traversal). */
    private static final Pattern SAFE_NAME = Pattern.compile("^[a-f0-9-]{36}\\.(jpg|png|webp|gif)$");

    private final ImageStorage storage;

    public ImageService(ImageStorage storage) {
        this.storage = storage;
    }

    /** Lưu ảnh và trả về đường dẫn dùng cho thẻ img, ví dụ /media/2f1c...-....jpg */
    public String upload(MultipartFile file) {
        String type = file.getContentType();
        String ext = type == null ? null : EXT.get(type.toLowerCase());
        if (ext == null) {
            throw new BusinessException("Chỉ nhận ảnh JPG, PNG, WEBP hoặc GIF");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("Ảnh tối đa 5MB");
        }
        String name = UUID.randomUUID() + "." + ext;
        try {
            storage.store(name, type, file.getBytes());
        } catch (IOException e) {
            throw new BusinessException("Không đọc được file ảnh");
        }
        return URL_PREFIX + name;
    }

    public Optional<ImageStorage.StoredImage> load(String name) {
        return SAFE_NAME.matcher(name).matches() ? storage.load(name) : Optional.empty();
    }

    static String typeOf(String name) {
        String ext = name.substring(name.lastIndexOf('.') + 1);
        return EXT.entrySet().stream().filter(e -> e.getValue().equals(ext))
                .map(Map.Entry::getKey).findFirst().orElse("application/octet-stream");
    }
}
