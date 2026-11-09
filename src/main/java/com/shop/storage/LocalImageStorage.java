package com.shop.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Lưu ảnh vào thư mục trên máy (mặc định ./uploads). Dùng khi chạy local / Docker Compose. */
@Component
@Profile("!prod")
public class LocalImageStorage implements ImageStorage {

    private final Path dir;

    public LocalImageStorage(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.dir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void store(String name, String contentType, byte[] data) {
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(name), data);
        } catch (IOException e) {
            throw new UncheckedIOException("Không lưu được ảnh", e);
        }
    }

    @Override
    public Optional<StoredImage> load(String name) {
        Path file = dir.resolve(name).normalize();
        if (!file.startsWith(dir) || !Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            String type = Files.probeContentType(file);
            return Optional.of(new StoredImage(Files.readAllBytes(file), type != null ? type : ImageService.typeOf(name)));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
