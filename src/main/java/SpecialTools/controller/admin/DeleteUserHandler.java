package SpecialTools.controller.admin;

import SpecialTools.controller.BaseHandler;
import SpecialTools.dao.UserDao;
import SpecialTools.service.TokenService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class DeleteUserHandler extends BaseHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeleteUserHandler.class);
    private final UserDao       userDao      = new UserDao();
    private final TokenService  tokenService = new TokenService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("🗑️ /admin/users/delete {}", exchange.getRequestMethod());
        if (!checkPostMethod(exchange, logger)) return;

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

        Map<String,String> params = parseRequestBody(exchange);
        try {
            int userId = Integer.parseInt(params.get("userId"));
            userDao.deleteOtpCodesByUser(userId);
            userDao.deleteUser(userId);
            sendResponse(exchange, 200, "User and OTP codes deleted", logger);
        } catch (Exception ex) {
            sendResponse(exchange, 400, "Invalid userId or deletion error", logger);
        }
    }
}