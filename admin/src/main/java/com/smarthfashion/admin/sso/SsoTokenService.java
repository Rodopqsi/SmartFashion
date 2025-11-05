package com.smarthfashion.admin.sso;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

public class SsoTokenService {
    private final String sharedSecret;
    private final long maxAgeSeconds;
    private final ObjectMapper mapper = new ObjectMapper();

    public SsoTokenService(String sharedSecret, long maxAgeSeconds) {
        this.sharedSecret = sharedSecret;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public record SsoPayload(String sub, String email, String role, long iat, long exp) {}

    public SsoPayload verify(String token) throws Exception {
        if (sharedSecret == null || sharedSecret.isBlank()) {
            throw new IllegalStateException("SSO is not configured");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) throw new IllegalArgumentException("Invalid token format");
        String payloadB64 = parts[0];
        String sigHex = parts[1];
        byte[] payloadBytes = Base64.getUrlDecoder().decode(payloadB64);
        String calc = hmacHex(payloadBytes, sharedSecret.getBytes(StandardCharsets.UTF_8));
        if (!calc.equalsIgnoreCase(sigHex)) throw new SecurityException("Invalid signature");
        ObjectNode node = (ObjectNode) mapper.readTree(payloadBytes);
        String sub = getAsString(node, "sub");
        String email = getAsString(node, "email");
        String role = getAsString(node, "role");
        long iat = getAsLong(node, "iat");
        long exp = getAsLong(node, "exp");
        long now = Instant.now().getEpochSecond();
        if (exp < now || (now - iat) > maxAgeSeconds + 5) {
            throw new SecurityException("Token expired");
        }
        return new SsoPayload(sub, email, role, iat, exp);
    }

    private static String hmacHex(byte[] payload, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        byte[] out = mac.doFinal(payload);
        StringBuilder sb = new StringBuilder(out.length * 2);
        for (byte b : out) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String getAsString(ObjectNode n, String k) {
        return n.hasNonNull(k) ? n.get(k).asText() : null;
        }
    private static long getAsLong(ObjectNode n, String k) {
        return n.hasNonNull(k) ? n.get(k).asLong() : 0L;
    }
}
