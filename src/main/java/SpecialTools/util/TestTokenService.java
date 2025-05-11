package SpecialTools.util;

import SpecialTools.service.TokenService;

public class TestTokenService {
    public static void main(String[] args) {
        TokenService tokenService = new TokenService();

        String token = tokenService.generateToken("admin", "ADMIN");
        System.out.println("🎫 Токен: " + token);

        boolean valid = tokenService.validateToken(token);
        System.out.println("✅ Валидный? " + valid);

        System.out.println("🔍 Логин: " + tokenService.extractLogin(token));
        System.out.println("🔍 Роль: " + tokenService.extractRole(token));
    }
}
