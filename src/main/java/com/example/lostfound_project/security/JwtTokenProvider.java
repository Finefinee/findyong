package com.example.lostfound_project.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String secretKey;
    private final long expirationMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-millis}") long expirationMillis) {
        this.secretKey = secretKey;
        this.expirationMillis = expirationMillis;
    }

    public String createToken(String userId) {
        try {
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = encodeJson(Map.of(
                    "sub", userId,
                    "exp", Instant.now().plusMillis(expirationMillis).getEpochSecond()
            ));
            String unsignedToken = header + "." + payload;
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception e) {
            throw new IllegalStateException("JWT 생성에 실패했습니다.", e);
        }
    }

    public String getUserId(String token) {
        if (!isValid(token)) {
            return null;
        }

        try {
            Map<String, Object> payload = parsePayload(token);
            return (String) payload.get("sub");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isValid(String token) {
        try {
            String[] parts = splitToken(token);
            String unsignedToken = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
                return false;
            }

            Map<String, Object> payload = parsePayload(token);
            Number expiration = (Number) payload.get("exp");
            return expiration != null && expiration.longValue() > Instant.now().getEpochSecond();
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private Map<String, Object> parsePayload(String token) throws Exception {
        String payload = splitToken(token)[1];
        byte[] decodedPayload = BASE64_URL_DECODER.decode(payload);
        return objectMapper.readValue(decodedPayload, new TypeReference<>() {
        });
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String[] splitToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("토큰이 없습니다.");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("JWT 형식이 올바르지 않습니다.");
        }

        return parts;
    }

    private boolean constantTimeEquals(String left, String right) {
        return left != null && right != null && MessageDigestUtil.equals(left, right);
    }

    private static class MessageDigestUtil {

        private static boolean equals(String left, String right) {
            byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
            byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
            if (leftBytes.length != rightBytes.length) {
                return false;
            }

            int result = 0;
            for (int i = 0; i < leftBytes.length; i++) {
                result |= leftBytes[i] ^ rightBytes[i];
            }
            return result == 0;
        }
    }
}
