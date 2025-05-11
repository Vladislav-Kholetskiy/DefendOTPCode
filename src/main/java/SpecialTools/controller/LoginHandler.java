package SpecialTools.controller;

import SpecialTools.dao.UserDao;
import SpecialTools.model.User;
import SpecialTools.util.PasswordHasher;
import SpecialTools.service.TokenService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class LoginHandler extends BaseHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    private final UserDao userDao = new UserDao();
    private final TokenService tokenService = new TokenService();
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("🔔 Получен запрос на /login с методом: {}", exchange.getRequestMethod());

        if (!checkPostMethod(exchange, logger)) {
            return;
        }

        Map<String, String> params = parseRequestBody(exchange);
        String login = params.get("login");
        String password = params.get("password");

        logger.info("🔍 Попытка авторизации пользователя: {}", login);
        if (login == null || password == null) {
            String errorJson = om.writeValueAsString(Map.of("error", "Логин и пароль обязательны"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 400, errorJson, logger);
            return;
        }

        Optional<User> userOpt = userDao.findByLogin(login);
        if (userOpt.isEmpty()) {
            String errorJson = om.writeValueAsString(Map.of("error", "Неверный логин или пароль"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 403, errorJson, logger);
            return;
        }

        User user = userOpt.get();
        // Verify hashed password
        String storedHash = user.getPassword();
        String inputHash = PasswordHasher.hash(password);
        if (!storedHash.equals(inputHash)) {
            String errorJson = om.writeValueAsString(Map.of("error", "Неверный логин или пароль"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 403, errorJson, logger);
            return;
        }

        String token = tokenService.generateToken(user.getLogin(), user.getRole());
        logger.info("✅ Пользователь {} успешно авторизован, токен выдан", login);
        String tokenJson = om.writeValueAsString(Map.of("token", token));
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, tokenJson, logger);
    }
}
