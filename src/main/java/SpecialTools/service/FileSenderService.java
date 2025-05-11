package SpecialTools.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSenderService {
    private final Path dir;

    public FileSenderService() {
        String dirEnv = System.getenv("FILE_DIR");
        if (dirEnv == null || dirEnv.isBlank()) {
            throw new IllegalStateException("Env FILE_DIR не задан");
        }
        this.dir = Paths.get(dirEnv);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку для OTP-файлов: " + dir, e);
        }
    }

    public void sendCodeToFile(String login, String code) {
        String fileName = "otp_" + login + "_" + System.currentTimeMillis() + ".txt";
        Path file = dir.resolve(fileName);
        try {
            System.out.println("Writing OTP to " + file);
            Files.writeString(file, code);
            System.out.println("Written OTP successfully");
        } catch (IOException e) {
            System.err.println("❌ Ошибка при записи файла: " + e.getMessage());
            throw new RuntimeException("Ошибка при записи OTP в файл", e);
        }
    }
}