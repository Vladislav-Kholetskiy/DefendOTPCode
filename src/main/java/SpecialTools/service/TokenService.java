package SpecialTools.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

public class TokenService {

    private static final String SECRET = "MySuperSecretKey123"; // можно потом вынести в конфиг
    private static final long EXPIRATION_SECONDS = 15 * 60; // токен живёт 15 минут

    public String generateToken(String login, String role) {
        long expiresAt = Instant.now().getEpochSecond() + EXPIRATION_SECONDS;

        String payload = login + ":" + role + ":" + expiresAt;
        String signature = sha256(payload + SECRET);

        String token = encode(login) + ":" + encode(role) + ":" + encode(String.valueOf(expiresAt)) + ":" + signature;
        return token;
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split(":");
            if (parts.length != 4) return false;

            String login = decode(parts[0]);
            String role = decode(parts[1]);
            String expires = decode(parts[2]);
            String signature = parts[3];

            String expectedSig = sha256(login + ":" + role + ":" + expires + SECRET);
            if (!expectedSig.equals(signature)) return false;

            long expiresAt = Long.parseLong(expires);
            return Instant.now().getEpochSecond() <= expiresAt;

        } catch (Exception e) {
            return false;
        }
    }

    public String extractLogin(String token) {
        return decode(token.split(":")[0]);
    }

    public String extractRole(String token) {
        return decode(token.split(":")[1]);
    }

    private String encode(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String encoded) {
        return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }
}
