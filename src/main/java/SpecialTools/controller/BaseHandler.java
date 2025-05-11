package SpecialTools.controller;

import SpecialTools.service.TokenService;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public abstract class BaseHandler {

    protected boolean checkPostMethod(HttpExchange exchange, Logger logger) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            logger.warn("⚠️ Метод {} не поддерживается", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1);
            return false;
        }
        return true;
    }

    protected Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return FormParser.parse(body);
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String responseText, Logger logger) throws IOException {
        logger.info("📤 Ответ отправлен с кодом {}: {}", statusCode, responseText);
        byte[] response = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected Optional<String> authenticateUser(HttpExchange exchange, Logger logger) throws IOException {
        if (!checkPostMethod(exchange, logger)) {
            return Optional.empty();
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("⚠️ Отсутствует заголовок Authorization");
            sendResponse(exchange, 401, "❌ Токен отсутствует", logger);
            return Optional.empty();
        }

        String token = authHeader.substring("Bearer ".length());
        TokenService tokenService = new TokenService();
        if (!tokenService.validateToken(token)) {
            logger.warn("⚠️ Недействительный токен: {}", token);
            sendResponse(exchange, 401, "❌ Недействительный токен", logger);
            return Optional.empty();
        }

        String login = tokenService.extractLogin(token);
        logger.info("🔑 Пользователь {} успешно аутентифицирован", login);
        return Optional.of(login);
    }

}
