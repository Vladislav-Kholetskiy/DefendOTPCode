package SpecialTools.controller.admin;

import SpecialTools.controller.BaseHandler;
import SpecialTools.dao.OtpCodeConfigDao;
import SpecialTools.service.TokenService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class UpdateOtpConfigHandler extends BaseHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateOtpConfigHandler.class);
    private final OtpCodeConfigDao configDao   = new OtpCodeConfigDao();
    private final TokenService    tokenService = new TokenService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("🔧 /admin/otp-config {}", exchange.getRequestMethod());
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
            int length = Integer.parseInt(params.get("length"));
            int ttl    = Integer.parseInt(params.get("ttl"));
            configDao.updateConfig(length, ttl);
            sendResponse(exchange, 200, "OTP configuration updated", logger);
        } catch (Exception ex) {
            sendResponse(exchange, 400, "Invalid parameters: length and ttl required", logger);
        }
    }
}