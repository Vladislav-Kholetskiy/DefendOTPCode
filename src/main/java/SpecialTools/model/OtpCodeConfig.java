package SpecialTools.model;

public class OtpCodeConfig {
    private int id;
    private int codeLength;
    private int lifeTimeSec;

    public OtpCodeConfig() {}

    public OtpCodeConfig(int id, int codeLength, int lifeTimeSec) {
        this.id = id;
        this.codeLength = codeLength;
        this.lifeTimeSec = lifeTimeSec;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCodeLength() { return codeLength; }
    public void setCodeLength(int codeLength) { this.codeLength = codeLength; }

    public int getLifeTimeSec() { return lifeTimeSec; }
    public void setLifeTimeSec(int lifeTimeSec) { this.lifeTimeSec = lifeTimeSec; }
}
