package com.lsdzs.lsdzs_tool.functiontest;

public class EbikeDetailResponse {

    private IotEbike iotEbike;

    public IotEbike getIotEbike() {
        return iotEbike;
    }

    public void setIotEbike(IotEbike iotEbike) {
        this.iotEbike = iotEbike;
    }

    public static class IotEbike {
        private Object storageTime;
        private String lng;
        private Object soc;
        private String cycleId;
        private Object alt;
        private Object remark;
        private String ebikeId;
        private Object type;
        private Object mac;
        private Object speed;
        private String ebikeCode;
        private Object lockStatus;
        private String gisTime;
        private Object keepliveTime;
        private String lat;
        private Integer status;

        public Object getStorageTime() {
            return storageTime;
        }

        public void setStorageTime(Object storageTime) {
            this.storageTime = storageTime;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }

        public Object getSoc() {
            return soc;
        }

        public void setSoc(Object soc) {
            this.soc = soc;
        }

        public String getCycleId() {
            return cycleId;
        }

        public void setCycleId(String cycleId) {
            this.cycleId = cycleId;
        }

        public Object getAlt() {
            return alt;
        }

        public void setAlt(Object alt) {
            this.alt = alt;
        }

        public Object getRemark() {
            return remark;
        }

        public void setRemark(Object remark) {
            this.remark = remark;
        }

        public String getEbikeId() {
            return ebikeId;
        }

        public void setEbikeId(String ebikeId) {
            this.ebikeId = ebikeId;
        }

        public Object getType() {
            return type;
        }

        public void setType(Object type) {
            this.type = type;
        }

        public Object getMac() {
            return mac;
        }

        public void setMac(Object mac) {
            this.mac = mac;
        }

        public Object getSpeed() {
            return speed;
        }

        public void setSpeed(Object speed) {
            this.speed = speed;
        }

        public String getEbikeCode() {
            return ebikeCode;
        }

        public void setEbikeCode(String ebikeCode) {
            this.ebikeCode = ebikeCode;
        }

        public Object getLockStatus() {
            return lockStatus;
        }

        public void setLockStatus(Object lockStatus) {
            this.lockStatus = lockStatus;
        }

        public String getGisTime() {
            return gisTime;
        }

        public void setGisTime(String gisTime) {
            this.gisTime = gisTime;
        }

        public Object getKeepliveTime() {
            return keepliveTime;
        }

        public void setKeepliveTime(Object keepliveTime) {
            this.keepliveTime = keepliveTime;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
