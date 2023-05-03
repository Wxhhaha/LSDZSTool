package com.lsdzs.lsdzs_tool.ble;

public class Device implements Cloneable{
    private String name;
    private String mac;
    private int rssi;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public Device clone() {
        Device device = null;
        try{
            device = (Device) super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return device;
    }
}
