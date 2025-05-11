package SpecialTools.util;

import SpecialTools.service.UserService;

import java.util.Optional;
import java.util.Scanner;

public class TestUserService {
    public static void main(String[] args) {
        UserService service = new UserService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("🔐 Вход:");
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        Optional<String> tokenOpt = service.login(login, password);

        if (tokenOpt.isPresent()) {
            System.out.println("🎫 Токен: " + tokenOpt.get());
        } else {
            System.out.println("❌ Неверные учётные данные.");
        }
    }
}
