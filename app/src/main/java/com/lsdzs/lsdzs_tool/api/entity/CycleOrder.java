package com.lsdzs.lsdzs_tool.api.entity;

public class CycleOrder {

    /**
     * cycleId
     */
    private String cycleId;
    /**
     * userId
     */
    private String userId;
    /**
     * startTime
     */
    private String startTime;
    /**
     * endTime
     */
    private String endTime;
    /**
     * ebikeId
     */
    private String ebikeId;
    /**
     * cycleStatus
     */
    private int cycleStatus;

    public String getCycleId() {
        return cycleId;
    }

    public void setCycleId(String cycleId) {
        this.cycleId = cycleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEbikeId() {
        return ebikeId;
    }

    public void setEbikeId(String ebikeId) {
        this.ebikeId = ebikeId;
    }

    public int getCycleStatus() {
        return cycleStatus;
    }

    public void setCycleStatus(int cycleStatus) {
        this.cycleStatus = cycleStatus;
    }
}
