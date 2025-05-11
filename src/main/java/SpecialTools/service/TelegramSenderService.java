package SpecialTools.service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TelegramSenderService {

    private final String botToken;
    private final String chatId;

    public TelegramSenderService() {
        Properties props = loadConfig();
        this.botToken = props.getProperty("telegram.token");
        this.chatId = props.getProperty("telegram.chat_id");
    }

    private Properties loadConfig() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("telegram.properties")) {
            Properties props = new Properties();
            props.load(is);
            return props;
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось загрузить telegram.properties", e);
        }
    }

    public void sendCode(String code) {
        try {
            String message = URLEncoder.encode("Ваш OTP-код: " + code, StandardCharsets.UTF_8);
            String urlStr = String.format(
                    "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s",
                    botToken, chatId, message
            );

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("📨 Отправлено в Telegram");
            } else {
                System.err.println("❌ Telegram API вернул: " + responseCode);
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка Telegram отправки: " + e.getMessage());
        }
    }
}
