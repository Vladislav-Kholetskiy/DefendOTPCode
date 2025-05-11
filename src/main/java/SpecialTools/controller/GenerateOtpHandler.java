package SpecialTools.controller;

import SpecialTools.dao.UserDao;
import SpecialTools.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class GenerateOtpHandler extends BaseHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(GenerateOtpHandler.class);

    private final OtpService otpService = new OtpService();
    private final UserDao userDao = new UserDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            logger.info("🔔 Получен запрос на /generate-otp с методом: {}", exchange.getRequestMethod());

            Optional<String> loginOpt = authenticateUser(exchange, logger);
            if (loginOpt.isEmpty()) return;
            String login = loginOpt.get();

            Map<String, String> params = parseRequestBody(exchange);
            String operationId = params.getOrDefault("operationId", "OP_DEFAULT");
            String channel     = params.getOrDefault("channel",     "file");

            int userId = userDao.findUserIdByLogin(login)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            String code = otpService.generateAndStoreOtp(userId, operationId);
            logger.info("📝 OTP-код сгенерирован для пользователя {} и операции {}", login, operationId);

            switch (channel.toLowerCase()) {
                case "email" -> {
                    new EmailSenderService().sendCode(login, code);
                    sendResponse(exchange, 200, "📧 OTP-код отправлен по Email", logger);
                }
                case "telegram" -> {
                    new TelegramSenderService().sendCode(code);
                    sendResponse(exchange, 200, "💬 OTP-код отправлен через Telegram", logger);
                }
                case "file" -> {
                    new FileSenderService().sendCodeToFile(login, code);
                    sendResponse(exchange, 200, "💾 OTP-код сохранён в файл", logger);
                }
                case "sms" -> {
                    new SmsSenderService().sendCode(login, code);
                    sendResponse(exchange, 200, "📲 OTP-код отправлен по SMS", logger);
                }
                default -> {
                    logger.warn("⚠️ Неизвестный канал отправки: {}", channel);
                    sendResponse(exchange, 400, "❌ Неизвестный канал: " + channel, logger);
                }
            }
        } catch (Exception e) {
            logger.error("❌ Внутренняя ошибка в GenerateOtpHandler: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "❌ Внутренняя ошибка сервера", logger);
        }
    }
}
