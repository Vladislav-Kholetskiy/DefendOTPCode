package SpecialTools.util;

import SpecialTools.service.SmsSenderService;

public class TestSmsSender {
    public static void main(String[] args) {
        SmsSenderService sender = new SmsSenderService();
        sender.sendCode("1234567890", "ABC123"); // номер — любой (для эмулятора)
    }
}
