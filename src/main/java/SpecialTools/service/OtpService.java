package SpecialTools.service;

import SpecialTools.dao.OtpCodeConfigDao;
import SpecialTools.dao.OtpCodeDao;
import SpecialTools.model.OtpCode;
import SpecialTools.model.OtpCodeConfig;
import SpecialTools.util.CodeGenerator;

import java.time.LocalDateTime;
import java.util.Optional;

public class OtpService {

    private final OtpCodeDao otpCodeDao = new OtpCodeDao();
    private final OtpCodeConfigDao configDao = new OtpCodeConfigDao();

    public String generateAndStoreOtp(int userId, String operationId) {
        OtpCodeConfig config = configDao.getConfig();
        String code = CodeGenerator.generate(config.getCodeLength());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusSeconds(config.getLifeTimeSec());
        otpCodeDao.insertOtpCode(userId, operationId, code, "ACTIVE", now, expires);
        return code;
    }


    public boolean validateOtp(int userId, String operationId, String inputCode) {
        Optional<OtpCode> optional = otpCodeDao.findActiveCodeByUserAndOperation(userId, operationId);

        if (optional.isEmpty()) {
            System.out.println("⚠️ Нет активного кода");
            return false;
        }

        OtpCode otp = optional.get();

        OtpCodeConfig config = configDao.getConfig();
        LocalDateTime expireTime = otp.getCreatedAt().plusSeconds(config.getLifeTimeSec());

        if (LocalDateTime.now().isAfter(expireTime)) {
            otpCodeDao.updateStatus(otp.getId(), "EXPIRED");
            System.out.println("⏰ Код просрочен");
            return false;
        }

        if (!otp.getCode().equalsIgnoreCase(inputCode)) {
            System.out.println("❌ Неверный код");
            return false;
        }

        otpCodeDao.updateStatus(otp.getId(), "USED");
        System.out.println("✅ Код успешно подтверждён");
        return true;
    }
}
