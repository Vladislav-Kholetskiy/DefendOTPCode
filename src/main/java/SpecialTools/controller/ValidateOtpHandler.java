package SpecialTools.controller;

import SpecialTools.dao.UserDao;
import SpecialTools.service.OtpService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ValidateOtpHandler extends BaseHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(ValidateOtpHandler.class);
    private final OtpService otpService = new OtpService();
    private final UserDao userDao = new UserDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("🔔 Получен запрос на /validate-otp с методом: {}", exchange.getRequestMethod());

        try {
            byte[] rawBody = exchange.getRequestBody().readAllBytes();
            String bodyStr = new String(rawBody, StandardCharsets.UTF_8);
            logger.info("BODY: {}", bodyStr);

            Optional<String> loginOpt = authenticateUser(exchange, logger);
            if (loginOpt.isEmpty()) return;
            String login = loginOpt.get();

            Map<String, String> params = new HashMap<>();
            for (String pair : bodyStr.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String val = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    params.put(key, val);
                }
            }

            String operationId = params.get("operationId");
            String otpCode = params.get("otpCode");
            logger.info("🔍 Пользователь {} пытается ввести OTP для операции {}", login, operationId);

            if (operationId == null || otpCode == null) {
                logger.warn("⚠️ Не указаны operationId или otpCode в запросе");
                sendResponse(exchange, 400, "❌ Необходимо указать operationId и otpCode", logger);
                return;
            }

            int userId = userDao.findUserIdByLogin(login)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + login));

            boolean valid = otpService.validateOtp(userId, operationId, otpCode);
            if (valid) {
                logger.info("✅ OTP-код успешно подтверждён для пользователя {} и операции {}", login, operationId);
                sendResponse(exchange, 200, "✅ OTP-код подтверждён", logger);
            } else {
                logger.warn("❌ Неверный или просроченный OTP-код для пользователя {} и операции {}", login, operationId);
                sendResponse(exchange, 403, "❌ Неверный или просроченный OTP-код", logger);
            }
        } catch (Throwable t) {
            logger.error("Exception in ValidateOtpHandler", t);
            sendResponse(exchange, 500, "Internal error", logger);
        }
    }
}
