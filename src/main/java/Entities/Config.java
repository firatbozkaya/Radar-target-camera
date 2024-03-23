package Entities;

public class Config {
    private static Config instance;
    private Config() {}
    public static Config getInstance(){
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }
    private int JFrameHeight = 600;
    private int runThreatPeriodMiliSecond = 500;
    private int sensorPublishmHZ = 1000; // 1Hz
    private int sensorListenmHZ = 1000; // 1Hz
    private int panelWidth = 500;
    private int panelHeight = 500;
    private int targetXVelocity = 10;
    private int targetYVelocity = 4;

    public void setJFrameHeight(int JFrameHeight) {
        this.JFrameHeight = JFrameHeight;
    }

    public int getJFrameHeight() {
        return JFrameHeight;
    }

    public void setRunThreatPeriodMiliSecond(int runThreatPeriodMiliSecond) {
        this.runThreatPeriodMiliSecond = runThreatPeriodMiliSecond;
    }

    public int getRunThreatPeriodMiliSecond() {
        return runThreatPeriodMiliSecond;
    }

    public int getSensorPublishmHZ() {
        return sensorPublishmHZ;
    }

    public int getSensorListenmHZ() {
        return sensorListenmHZ;
    }

    public void setSensorPublishmHZ(int sensorPublishmHZ) {
        this.sensorPublishmHZ = sensorPublishmHZ;
    }

    public void setSensorListenmHZ(int sensorListenmHZ) {
        this.sensorListenmHZ = sensorListenmHZ;
    }

    public int getPanelWidth() {
        return panelWidth;
    }

    public int getPanelHeight() {
        return panelHeight;
    }

    public int getTargetXVelocity() {
        return targetXVelocity;
    }

    public int getTargetYVelocity() {
        return targetYVelocity;
    }

    public void setPanelWidth(int panelWidth) {
        this.panelWidth = panelWidth;
    }

    public void setPanelHeight(int panelHeight) {
        this.panelHeight = panelHeight;
    }

    public void setTargetXVelocity(int targetXVelocity) {
        this.targetXVelocity = targetXVelocity;
    }

    public void setTargetYVelocity(int targetYVelocity) {
        this.targetYVelocity = targetYVelocity;
    }
}
