package SpecialTools.controller;

import SpecialTools.dao.UserDao;
import SpecialTools.model.User;
import SpecialTools.util.PasswordHasher;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class RegisterHandler extends BaseHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(RegisterHandler.class);
    private final UserDao userDao = new UserDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("🔔 Получен запрос на /register с методом: {}", exchange.getRequestMethod());

        if (!checkPostMethod(exchange, logger)) {
            return;
        }

        Map<String, String> params = parseRequestBody(exchange);
        String login = params.get("login");
        String password = params.get("password");
        String role = params.get("role");

        logger.info("🔍 Пытаемся зарегистрировать пользователя: {}, роль: {}", login, role);

        if (login == null || password == null || role == null) {
            logger.warn("⚠️ Отсутствуют логин, пароль или роль");
            sendResponse(exchange, 400, "❌ Требуется login, password и role", logger);
            return;
        }

        if ("ADMIN".equalsIgnoreCase(role) && userDao.adminExists()) {
            logger.warn("⚠️ Попытка зарегистрировать второго администратора");
            sendResponse(exchange, 403, "❌ Администратор уже существует", logger);
            return;
        }

        try {
            // Hash password before saving
            String hashed = PasswordHasher.hash(password);
            User user = new User();
            user.setLogin(login);
            user.setPassword(hashed);
            user.setRole(role.toUpperCase());

            userDao.createUser(user);
            logger.info("✅ Пользователь {} успешно зарегистрирован с ролью {}", login, role);
            sendResponse(exchange, 200, "✅ Пользователь зарегистрирован", logger);
        } catch (Exception e) {
            logger.error("❌ Ошибка при сохранении пользователя в БД: {}", e.getMessage());
            sendResponse(exchange, 500, "❌ Внутренняя ошибка сервера", logger);
        }
    }
}
