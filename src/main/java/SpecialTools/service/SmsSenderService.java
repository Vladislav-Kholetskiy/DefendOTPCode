package SpecialTools.service;

import org.smpp.Connection;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.SubmitSM;

import java.io.InputStream;
import java.util.Properties;

public class SmsSenderService {

    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsSenderService() {
        Properties props = loadConfig();
        this.host = props.getProperty("smpp.host");
        this.port = Integer.parseInt(props.getProperty("smpp.port"));
        this.systemId = props.getProperty("smpp.system_id");
        this.password = props.getProperty("smpp.password");
        this.systemType = props.getProperty("smpp.system_type");
        this.sourceAddress = props.getProperty("smpp.source_addr");
    }

    private Properties loadConfig() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sms.properties")) {
            Properties props = new Properties();
            props.load(is);
            return props;
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось загрузить sms.properties", e);
        }
    }

    public void sendCode(String destination, String code) {
        Connection connection;
        Session session;

        try {
            // 1. Установка соединения
            connection = new TCPIPConnection(host, port);
            session = new Session(connection);

            // 2. Привязка (bind)
            BindTransmitter bindRequest = new BindTransmitter();
            bindRequest.setSystemId(systemId);
            bindRequest.setPassword(password);
            bindRequest.setSystemType(systemType);
            bindRequest.setInterfaceVersion((byte) 0x34); // SMPP 3.4
            bindRequest.setAddressRange(sourceAddress);

            BindResponse bindResponse = session.bind(bindRequest);
            if (bindResponse.getCommandStatus() != 0) {
                throw new RuntimeException("❌ Bind failed: " + bindResponse.getCommandStatus());
            }

            // 3. Отправка OTP
            SubmitSM submit = new SubmitSM();
            submit.setSourceAddr(sourceAddress);
            submit.setDestAddr(destination);
            submit.setShortMessage("Ваш OTP-код: " + code);

            session.submit(submit);
            System.out.println("📲 SMS отправлено на " + destination);

        } catch (Exception e) {
            System.err.println("❌ Ошибка при отправке SMS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}