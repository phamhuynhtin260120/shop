package com.shop.storage;

import com.shop.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lưu ảnh vào Google Cloud Storage qua JSON API.
 * Xác thực bằng service account của Cloud Run (shop-run): lấy access token từ metadata server,
 * nên không cần file khóa hay mật khẩu nào. Bucket để riêng tư, ảnh được trả qua MediaController.
 */
@Component
@Profile("prod")
public class GcsImageStorage implements ImageStorage {

    private static final String TOKEN_URL =
            "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token";
    private static final Pattern TOKEN = Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern EXPIRES = Pattern.compile("\"expires_in\"\\s*:\\s*(\\d+)");

    private final String bucket;
    private final RestClient http = RestClient.create();
    private String token;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public GcsImageStorage(@Value("${app.gcs-bucket}") String bucket) {
        this.bucket = bucket;
    }

    @Override
    public void store(String name, String contentType, byte[] data) {
        if (bucket == null || bucket.isBlank()) {
            throw new BusinessException("Chưa cấu hình bucket lưu ảnh (biến môi trường GCS_BUCKET)");
        }
        http.post()
                .uri("https://storage.googleapis.com/upload/storage/v1/b/" + bucket
                        + "/o?uploadType=media&name=" + encode(name))
                .header("Authorization", "Bearer " + accessToken())
                .header("Content-Type", contentType)
                .body(data)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public Optional<StoredImage> load(String name) {
        try {
            ResponseEntity<byte[]> res = http.get()
                    .uri("https://storage.googleapis.com/storage/v1/b/" + bucket + "/o/" + encode(name) + "?alt=media")
                    .header("Authorization", "Bearer " + accessToken())
                    .retrieve()
                    .toEntity(byte[].class);
            String type = res.getHeaders().getFirst("Content-Type");
            return Optional.of(new StoredImage(res.getBody(), type != null ? type : ImageService.typeOf(name)));
        } catch (RestClientResponseException e) {
            return Optional.empty();       // 404: không có ảnh
        }
    }

    /** Token có hạn khoảng 1 giờ; lưu lại và chỉ xin token mới khi sắp hết hạn. */
    private synchronized String accessToken() {
        if (token == null || Instant.now().isAfter(tokenExpiresAt)) {
            String json = http.get().uri(TOKEN_URL).header("Metadata-Flavor", "Google")
                    .retrieve().body(String.class);
            Matcher t = TOKEN.matcher(json == null ? "" : json);
            if (!t.find()) {
                throw new IllegalStateException("Không lấy được access token từ metadata server");
            }
            Matcher e = EXPIRES.matcher(json);
            long seconds = e.find() ? Long.parseLong(e.group(1)) : 300;
            token = t.group(1);
            tokenExpiresAt = Instant.now().plusSeconds(Math.max(60, seconds - 60));
        }
        return token;
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
