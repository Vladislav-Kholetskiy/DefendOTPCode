package SpecialTools.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

public class EmailSenderService {

    private final Session session;
    private final String fromEmail;

    public EmailSenderService() {
        Properties config = loadConfig();
        String username = config.getProperty("email.username");
        String password = config.getProperty("email.password");
        fromEmail = config.getProperty("email.from");

        session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Properties loadConfig() {
        try {
            Properties props = new Properties();
            props.load(EmailSenderService.class.getClassLoader().getResourceAsStream("email.properties"));
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить email.properties", e);
        }
    }

    public void sendCode(String toEmail, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Ваш OTP-код");
            message.setText("Ваш OTP-код: " + code);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Ошибка при отправке email", e);
        }
    }
}
