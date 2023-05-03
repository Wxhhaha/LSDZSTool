package com.lsdzs.lsdzs_tool.ble;

public class SettingValue {
	private int protocolType;//协议类型
	private int unit;// 公里、英里切换，0：公里，1：英里
	private int lightStatus;// 大灯状态，0：关 ，1：开
	private int walkMode;// 6km推行状态 0：无 ，1：有
	private int wheelPerimeter;// 轮径 0:16 inch, 1:18inch, 2:20inch, 3:22inch,
								// 4:24inch, 5:26inch, 6:700c, 7:28inch
	private int speedLimit;// 限速值 km/h,如果界面上设的是英里，要先转换成公里，再传值
	private int speedSensorMagnets;// 每圈测速磁钢数
	private int batteryCapacity;// 电池容量
	private int fullPowerVoltage;// 满电电压值，实际值x10, 380代表38V
	private int minPowerVoltage;// 欠压值，实际值x10, 300代表30V
	private int currentLimit;// 限流值，实际值x10, 150代表15A
	private int startupAcceleration;// 缓启动参数，3：20%，2：40%，1：70%，0：100%
	private int totalPasLevels;// 总档位
	private int currentGear;// 当前档位值，0~9
	private int levelLimit1;// 1档最大百分比，40代表40%
	private int levelLimit2;// 2档最大百分比，55代表55%
	private int levelLimit3;// 3档最大百分比，70代表70%
	private int levelLimit4;// 4档最大百分比，85代表85%
	private int levelLimit5; // 5档最大百分比，100代表100%
	private int levelLimit6;// 6档最大百分比，100代表100%
	private int levelLimit7;// 7档最大百分比，100代表100%
	private int levelLimit8;// 8档最大百分比，100代表100%
	private int levelLimit9;// 9档最大百分比，100代表100%
	private int pedalSensorMagnets;// 助力传感器一圈脉冲数
	private int pedalPulseDutyCycle; // 助力方向<50% 为0，>50%为1
	private int startAfterMagnets;// 启动等待过磁钢数
	private int throttleMaxSpeed;// 转把6km/h限速，0不限，1限
	private int throttleLimitedByPASLevels;// 转把分档与否，0不分，1分
	private int pasType;// 档位类型 0：速度档 1：电流档

	public int getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}

	public int getUnit() {
		return unit;
	}

	public void setUnit(int unit) {
		this.unit = unit;
	}

	public int getLightStatus() {
		return lightStatus;
	}

	public void setLightStatus(int lightStatus) {
		this.lightStatus = lightStatus;
	}

	public int getWalkMode() {
		return walkMode;
	}

	public void setWalkMode(int walkMode) {
		this.walkMode = walkMode;
	}

	public int getWheelPerimeter() {
		return wheelPerimeter;
	}

	public void setWheelPerimeter(int wheelPerimeter) {
		this.wheelPerimeter = wheelPerimeter;
	}

	public int getSpeedLimit() {
		return speedLimit;
	}

	public void setSpeedLimit(int speedLimit) {
		this.speedLimit = speedLimit;
	}

	public int getSpeedSensorMagnets() {
		return speedSensorMagnets;
	}

	public void setSpeedSensorMagnets(int speedSensorMagnets) {
		this.speedSensorMagnets = speedSensorMagnets;
	}

	public int getBatteryCapacity() {
		return batteryCapacity;
	}

	public void setBatteryCapacity(int batteryCapacity) {
		this.batteryCapacity = batteryCapacity;
	}

	public int getFullPowerVoltage() {
		return fullPowerVoltage;
	}

	public void setFullPowerVoltage(int fullPowerVoltage) {
		this.fullPowerVoltage = fullPowerVoltage;
	}

	public int getMinPowerVoltage() {
		return minPowerVoltage;
	}

	public void setMinPowerVoltage(int minPowerVoltage) {
		this.minPowerVoltage = minPowerVoltage;
	}

	public int getCurrentLimit() {
		return currentLimit;
	}

	public void setCurrentLimit(int currentLimit) {
		this.currentLimit = currentLimit;
	}

	public int getStartupAcceleration() {
		return startupAcceleration;
	}

	public void setStartupAcceleration(int startupAcceleration) {
		this.startupAcceleration = startupAcceleration;
	}

	public int getTotalPasLevels() {
		return totalPasLevels;
	}

	public void setTotalPasLevels(int totalPasLevels) {
		this.totalPasLevels = totalPasLevels;
	}

	public int getCurrentGear() {
		return currentGear;
	}

	public void setCurrentGear(int currentGear) {
		this.currentGear = currentGear;
	}

	public int getLevelLimit1() {
		return levelLimit1;
	}

	public void setLevelLimit1(int levelLimit1) {
		this.levelLimit1 = levelLimit1;
	}

	public int getLevelLimit2() {
		return levelLimit2;
	}

	public void setLevelLimit2(int levelLimit2) {
		this.levelLimit2 = levelLimit2;
	}

	public int getLevelLimit3() {
		return levelLimit3;
	}

	public void setLevelLimit3(int levelLimit3) {
		this.levelLimit3 = levelLimit3;
	}

	public int getLevelLimit4() {
		return levelLimit4;
	}

	public void setLevelLimit4(int levelLimit4) {
		this.levelLimit4 = levelLimit4;
	}

	public int getLevelLimit5() {
		return levelLimit5;
	}

	public void setLevelLimit5(int levelLimit5) {
		this.levelLimit5 = levelLimit5;
	}

	public int getLevelLimit6() {
		return levelLimit6;
	}

	public void setLevelLimit6(int levelLimit6) {
		this.levelLimit6 = levelLimit6;
	}

	public int getLevelLimit7() {
		return levelLimit7;
	}

	public void setLevelLimit7(int levelLimit7) {
		this.levelLimit7 = levelLimit7;
	}

	public int getLevelLimit8() {
		return levelLimit8;
	}

	public void setLevelLimit8(int levelLimit8) {
		this.levelLimit8 = levelLimit8;
	}

	public int getLevelLimit9() {
		return levelLimit9;
	}

	public void setLevelLimit9(int levelLimit9) {
		this.levelLimit9 = levelLimit9;
	}

	public int getPedalSensorMagnets() {
		return pedalSensorMagnets;
	}

	public void setPedalSensorMagnets(int pedalSensorMagnets) {
		this.pedalSensorMagnets = pedalSensorMagnets;
	}

	public int getPedalPulseDutyCycle() {
		return pedalPulseDutyCycle;
	}

	public void setPedalPulseDutyCycle(int pedalPulseDutyCycle) {
		this.pedalPulseDutyCycle = pedalPulseDutyCycle;
	}

	public int getStartAfterMagnets() {
		return startAfterMagnets;
	}

	public void setStartAfterMagnets(int startAfterMagnets) {
		this.startAfterMagnets = startAfterMagnets;
	}

	public int getThrottleMaxSpeed() {
		return throttleMaxSpeed;
	}

	public void setThrottleMaxSpeed(int throttleMaxSpeed) {
		this.throttleMaxSpeed = throttleMaxSpeed;
	}

	public int getThrottleLimitedByPASLevels() {
		return throttleLimitedByPASLevels;
	}

	public void setThrottleLimitedByPASLevels(int throttleLimitedByPASLevels) {
		this.throttleLimitedByPASLevels = throttleLimitedByPASLevels;
	}

	public int getPasType() {
		return pasType;
	}

	public void setPasType(int pasType) {
		this.pasType = pasType;
	}
}
