package SpecialTools.util;

import SpecialTools.config.DbConnectionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DbConnectionFactory.getConnection()) {
            System.out.println("✅ Соединение установлено!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT version();");

            if (rs.next()) {
                System.out.println("PostgreSQL версия: " + rs.getString(1));
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка подключения к базе данных:");
            e.printStackTrace();
        }
    }
}
