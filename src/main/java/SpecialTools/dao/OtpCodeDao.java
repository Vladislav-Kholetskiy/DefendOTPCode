// src/main/java/SpecialTools/dao/OtpCodeDao.java
package SpecialTools.dao;

import SpecialTools.config.DbConnectionFactory;
import SpecialTools.model.OtpCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class OtpCodeDao {

    private static final String INSERT_SQL = """
        INSERT INTO otp_codes
          (user_id, operation_id, code, status, created_at, expires_at)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    private static final String FIND_ACTIVE_SQL = """
        SELECT id, user_id, operation_id, code, status, created_at, expires_at
          FROM otp_codes
         WHERE user_id = ?
           AND LOWER(operation_id) = LOWER(?)
           AND status = 'ACTIVE'
           AND expires_at > now()
        """;

    private static final String UPDATE_STATUS_SQL = """
        UPDATE otp_codes
           SET status = ?
         WHERE id = ?
        """;

    /** Вставляет новый OTP и рассчитывает expires_at = createdAt + lifeTimeSec */
    public void insertOtpCode(int userId,
                              String operationId,
                              String code,
                              String status,
                              LocalDateTime createdAt,
                              LocalDateTime expiresAt) {
        try (Connection conn = DbConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setInt(1, userId);
            ps.setString(2, operationId);
            ps.setString(3, code);
            ps.setString(4, status);
            ps.setTimestamp(5, Timestamp.valueOf(createdAt));
            ps.setTimestamp(6, Timestamp.valueOf(expiresAt));

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при вставке OTP-кода", e);
        }
    }

    public Optional<OtpCode> findActiveCodeByUserAndOperation(int userId, String operationId) {
        try (Connection conn = DbConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ACTIVE_SQL)) {

            ps.setInt(1, userId);
            ps.setString(2, operationId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OtpCode otp = new OtpCode(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("operation_id"),
                            rs.getString("code"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("expires_at").toLocalDateTime()
                    );
                    return Optional.of(otp);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске активного OTP-кода", e);
        }
    }

    public void updateStatus(int id, String newStatus) {
        try (Connection conn = DbConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS_SQL)) {

            ps.setString(1, newStatus);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении статуса OTP-кода", e);
        }
    }
}
