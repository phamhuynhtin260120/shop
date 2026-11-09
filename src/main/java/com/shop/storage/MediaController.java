package com.shop.storage;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/** Trả ảnh đã tải lên: /media/{tên file}. Trình duyệt được phép lưu đệm 7 ngày. */
@RestController
public class MediaController {

    private final ImageService imageService;

    public MediaController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/media/{name:.+}")
    public ResponseEntity<byte[]> get(@PathVariable String name) {
        return imageService.load(name)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.contentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                        .body(img.data()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
