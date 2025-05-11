package SpecialTools.dao;

import SpecialTools.config.DbConnectionFactory;
import SpecialTools.model.OtpCodeConfig;

import java.sql.*;

public class OtpCodeConfigDao {
    private static final int DEFAULT_TTL_SEC = 300;
    private static final int DEFAULT_CODE_LENGTH = 6;

    public OtpCodeConfig getConfig() {
        String sql = "SELECT * FROM otp_code_config WHERE id = 1";
        try (Connection conn = DbConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new OtpCodeConfig(
                        rs.getInt("id"),
                        rs.getInt("code_length"),
                        rs.getInt("life_time_sec")
                );
            } else {
                throw new RuntimeException("Конфигурация OTP не найдена (id = 1)");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении конфигурации OTP", e);
        }
    }

    private void ensureDefaultConfig() {
        String checkSql = "SELECT COUNT(*) FROM otp_code_config";
        String insertSql = """
                INSERT INTO otp_code_config (id, life_time_sec, code_length)
                VALUES (1, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """;
        try (Connection conn = DbConnectionFactory.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(checkSql);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                        insert.setInt(1, DEFAULT_TTL_SEC);
                        insert.setInt(2, DEFAULT_CODE_LENGTH);
                        insert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при инициализации otp_code_config", e);
        }
    }

    public void updateConfig(OtpCodeConfig config) {
        String sql = "UPDATE otp_code_config SET code_length = ?, life_time_sec = ? WHERE id = 1";
        try (Connection conn = DbConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, config.getCodeLength());
            ps.setInt(2, config.getLifeTimeSec());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении конфигурации OTP", e);
        }
    }

    public void updateConfig(int codeLength, int lifeTimeSec) {
        String sql = "UPDATE otp_code_config SET code_length = ?, life_time_sec = ? WHERE id = 1";
        try (Connection conn = DbConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, codeLength);
            ps.setInt(2, lifeTimeSec);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении конфигурации OTP", e);
        }
    }

    public void insertDefaultIfNotExists() {
        String sql = "INSERT INTO otp_code_config (id, code_length, life_time_sec) " +
                "VALUES (1, 6, 300) ON CONFLICT (id) DO NOTHING";
        try (Connection conn = DbConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при инициализации конфигурации OTP", e);
        }
    }
}
