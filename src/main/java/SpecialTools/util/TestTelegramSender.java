package SpecialTools.util;

import SpecialTools.service.TelegramSenderService;

public class TestTelegramSender {
    public static void main(String[] args) {
        new TelegramSenderService().sendCode("ABC123");
    }
}
