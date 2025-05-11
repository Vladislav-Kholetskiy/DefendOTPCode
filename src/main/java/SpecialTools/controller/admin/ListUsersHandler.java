package SpecialTools.controller.admin;

import SpecialTools.controller.BaseHandler;
import SpecialTools.dao.UserDao;
import SpecialTools.model.User;
import SpecialTools.service.TokenService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ListUsersHandler extends BaseHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(ListUsersHandler.class);
    private final UserDao userDao = new UserDao();
    private final TokenService tokenService = new TokenService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("📋 /admin/users {}", exchange.getRequestMethod());
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "Недопустимый метод", logger);
            return;
        }

        Optional<String> loginOpt = authenticateUser(exchange, logger);
        if (loginOpt.isEmpty()) return;

        String token = exchange.getRequestHeaders()
                .getFirst("Authorization")
                .substring("Bearer ".length());
        String role = tokenService.extractRole(token);
        if (!"ADMIN".equals(role)) {
            sendResponse(exchange, 403, "Forbidden: admins only", logger);
            return;
        }

        try {
            List<User> users = userDao.findAllExceptAdmins();
            String json = mapper.writeValueAsString(users);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, json, logger);
        } catch (Exception ex) {
            sendResponse(exchange, 500, "Error fetching users", logger);
        }
    }
}