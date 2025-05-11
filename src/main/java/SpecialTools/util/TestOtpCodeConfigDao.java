package SpecialTools.util;

import SpecialTools.dao.OtpCodeConfigDao;
import SpecialTools.model.OtpCodeConfig;

public class TestOtpCodeConfigDao {
    public static void main(String[] args) {
        OtpCodeConfigDao dao = new OtpCodeConfigDao();

        dao.insertDefaultIfNotExists();

        OtpCodeConfig config = dao.getConfig();
        System.out.println("🔧 Текущая конфигурация:");
        System.out.println(" - длина кода: " + config.getCodeLength());
        System.out.println(" - время жизни: " + config.getLifeTimeSec() + " сек.");

        config.setCodeLength(8);
        config.setLifeTimeSec(600);
        dao.updateConfig(config);

        System.out.println("✅ Конфигурация обновлена.");
    }
}
