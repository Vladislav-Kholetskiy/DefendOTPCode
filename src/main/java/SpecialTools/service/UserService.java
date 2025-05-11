package SpecialTools.service;

import SpecialTools.dao.UserDao;
import SpecialTools.model.User;
import SpecialTools.util.PasswordHasher;

import java.util.Optional;

public class UserService {

    private final UserDao userDao = new UserDao();
    private final TokenService tokenService = new TokenService();

    public boolean register(String login, String password, String role) {
        Optional<User> existing = userDao.findByLogin(login);
        if (existing.isPresent()) {
            System.out.println("⚠️ Пользователь уже существует");
            return false;
        }

        if (role.equalsIgnoreCase("ADMIN") && userDao.adminExists()) {
            System.out.println("❌ Администратор уже существует");
            return false;
        }

        String hashedPassword = PasswordHasher.hash(password);
        User user = new User(0, login, hashedPassword, role.toUpperCase());

        userDao.createUser(user);
        System.out.println("✅ Пользователь зарегистрирован: " + login + " [" + role + "]");
        return true;
    }

    public Optional<String> login(String login, String password) {
        Optional<User> userOpt = userDao.findByLogin(login);
        if (userOpt.isEmpty()) {
            System.out.println("❌ Логин не найден");
            return Optional.empty();
        }

        User user = userOpt.get();
        String hashed = PasswordHasher.hash(password);

        if (!hashed.equals(user.getPassword())) {
            System.out.println("❌ Неверный пароль");
            return Optional.empty();
        }

        String token = tokenService.generateToken(user.getLogin(), user.getRole());
        System.out.println("✅ Авторизация успешна. Токен выдан.");
        return Optional.of(token);
    }
}
