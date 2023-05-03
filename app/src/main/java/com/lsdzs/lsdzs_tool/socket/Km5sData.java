package com.lsdzs.lsdzs_tool.socket;

public class Km5sData {

    /**
     * 车辆编号必填字符型
     */
    private String ebikeId;
    /**
     * 必填车辆编号字符
     */
    private String cycleId;
    /**
     * 速度
     */
    private String speed;
    /**
     * 功率百分比
     */
    private String pwrp;
    /**
     * 功率值
     */
    private String pwrv;
    /**
     * 剩余电量
     */
    private String soc;
    /**
     * 剩余里程
     */
    private String rod;
    /**
     * 6KM推行
     */
    private String swkm6;
    /**
     * 电流
     */
    private String cur;
    /**
     * 大灯
     */
    private String lamp;
    /**
     * 故障代码
     */
    private String fault;

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

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getPwrp() {
        return pwrp;
    }

    public void setPwrp(String pwrp) {
        this.pwrp = pwrp;
    }

    public String getPwrv() {
        return pwrv;
    }

    public void setPwrv(String pwrv) {
        this.pwrv = pwrv;
    }

    public String getSoc() {
        return soc;
    }

    public void setSoc(String soc) {
        this.soc = soc;
    }

    public String getRod() {
        return rod;
    }

    public void setRod(String rod) {
        this.rod = rod;
    }

    public String getSwkm6() {
        return swkm6;
    }

    public void setSwkm6(String swkm6) {
        this.swkm6 = swkm6;
    }

    public String getCur() {
        return cur;
    }

    public void setCur(String cur) {
        this.cur = cur;
    }

    public String getLamp() {
        return lamp;
    }

    public void setLamp(String lamp) {
        this.lamp = lamp;
    }

    public String getFault() {
        return fault;
    }

    public void setFault(String fault) {
        this.fault = fault;
    }
}
