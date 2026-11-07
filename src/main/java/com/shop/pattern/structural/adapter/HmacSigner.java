package com.shop.pattern.structural.adapter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/** Ký dữ liệu bằng HMAC-SHA512 (cách các cổng thanh toán xác thực request). */
public final class HmacSigner {

    private HmacSigner() { }

    public static String sign(String secret, Map<String, String> params) {
        StringBuilder data = new StringBuilder();
        new TreeMap<>(params).forEach((k, v) -> data.append(k).append('=').append(v).append('&'));
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Không ký được dữ liệu thanh toán", e);
        }
    }
}
