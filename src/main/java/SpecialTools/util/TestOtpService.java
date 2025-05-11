package SpecialTools.util;

import SpecialTools.service.OtpService;

import java.util.Scanner;

public class TestOtpService {
    public static void main(String[] args) {
        OtpService service = new OtpService();

        int userId = 1;
        String operationId = "TEST-OTP";

        String code = service.generateAndStoreOtp(userId, operationId);
        System.out.println("📨 Сгенерированный код: " + code);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите код для валидации: ");
        String input = scanner.nextLine();

        boolean result = service.validateOtp(userId, operationId, input);
        System.out.println("Результат: " + (result ? "Успех ✅" : "Ошибка ❌"));
    }
}

