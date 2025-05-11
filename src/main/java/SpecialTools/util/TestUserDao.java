package SpecialTools.util;

import SpecialTools.dao.UserDao;
import SpecialTools.model.User;

import java.util.Optional;

public class TestUserDao {
    public static void main(String[] args) {
        UserDao userDao = new UserDao();

        if (!userDao.adminExists()) {
            User admin = new User(0, "admin", "hashed_password", "ADMIN");
            userDao.createUser(admin);
            System.out.println("✅ Админ создан");
        } else {
            System.out.println("⚠️ Админ уже существует");
        }

        Optional<User> userOpt = userDao.findByLogin("admin");
        userOpt.ifPresent(u -> System.out.println("🔍 Найден: " + u.getLogin() + ", роль: " + u.getRole()));
    }
}
