package com.lsdzs.lsdzs_tool.socket;

public class GyroData {
    private String ebikeId;
    private String cycleId;
    private long recTime;
    private String gyroYaw;
    private String gyroPitc;
    private String gyroRoll;

    public long getRecTime() {
        return recTime;
    }

    public void setRecTime(long recTime) {
        this.recTime = recTime;
    }

    public String getEbikeId() {
        return ebikeId;
    }

    public void setEbikeId(String ebikeId) {
        this.ebikeId = ebikeId;
    }

    public String getCycleId() {
        return cycleId;
    }

    public void setCycleId(String cycleId) {
        this.cycleId = cycleId;
    }

    public String getGyroYaw() {
        return gyroYaw;
    }

    public void setGyroYaw(String gyroYaw) {
        this.gyroYaw = gyroYaw;
    }

    public String getGyroPitc() {
        return gyroPitc;
    }

    public void setGyroPitc(String gyroPitc) {
        this.gyroPitc = gyroPitc;
    }

    public String getGyroRoll() {
        return gyroRoll;
    }

    public void setGyroRoll(String gyroRoll) {
        this.gyroRoll = gyroRoll;
    }
}
