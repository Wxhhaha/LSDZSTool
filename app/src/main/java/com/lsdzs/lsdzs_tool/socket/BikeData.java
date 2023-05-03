package com.lsdzs.lsdzs_tool.socket;

public class BikeData {

    private String ebikeId;
    private String cycleId;
    private Object batteryTemperature;
    private Object controllerTemperature;
    private Object motorTemperature;
    private Integer altitude;
    private Integer current;
    private Integer cadence;
    private String lat;
    private String lng;
    private Integer power;
    private Integer speed;
    private Integer voltage;
    private Integer mileage;
    private Long recTime;
    private String faultId;
    private int lamp;
    private int swkm6;
    private int brake;
    private int soc;

    public int getSoc() {
        return soc;
    }

    public void setSoc(int soc) {
        this.soc = soc;
    }

    public int getLamp() {
        return lamp;
    }

    public void setLamp(int lamp) {
        this.lamp = lamp;
    }

    public int getSwkm6() {
        return swkm6;
    }

    public void setSwkm6(int swkm6) {
        this.swkm6 = swkm6;
    }

    public int getBrake() {
        return brake;
    }

    public void setBrake(int brake) {
        this.brake = brake;
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

    public Object getBatteryTemperature() {
        return batteryTemperature;
    }

    public void setBatteryTemperature(Object batteryTemperature) {
        this.batteryTemperature = batteryTemperature;
    }

    public Object getControllerTemperature() {
        return controllerTemperature;
    }

    public void setControllerTemperature(Object controllerTemperature) {
        this.controllerTemperature = controllerTemperature;
    }

    public Object getMotorTemperature() {
        return motorTemperature;
    }

    public void setMotorTemperature(Object motorTemperature) {
        this.motorTemperature = motorTemperature;
    }

    public Integer getAltitude() {
        return altitude;
    }

    public void setAltitude(Integer altitude) {
        this.altitude = altitude;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getCadence() {
        return cadence;
    }

    public void setCadence(Integer cadence) {
        this.cadence = cadence;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getVoltage() {
        return voltage;
    }

    public void setVoltage(Integer voltage) {
        this.voltage = voltage;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public Long getRecTime() {
        return recTime;
    }

    public void setRecTime(Long recTime) {
        this.recTime = recTime;
    }

    public String getFaultId() {
        return faultId;
    }

    public void setFaultId(String faultId) {
        this.faultId = faultId;
    }
}
