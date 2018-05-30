package com.ckt.testauxiliarytool.batterymonitor.bean;


/**
 * 封装电池信息的实体类，需要继承RealmObject，用于Realm数据库映射
 */
public class BatteryInfo {
    private long currentMillis; // 当前时间戳
    private int level; // 当前电量
    private String voltage; // 电压
    private float temperature; // 温度
    private String chargeCurrent; // 充电电流
    private String health; // 电池的健康状态
    private String status; // 电池的充放电状态
    private String plugged; // 充电方式：AC电源、USB、无线

    public BatteryInfo() {

    }

    public BatteryInfo(long currentMillis, int level, String voltage, float temperature, String chargeCurrent, String health, String status, String plugged) {
        this.currentMillis = currentMillis;
        this.level = level;
        this.voltage = voltage;
        this.temperature = temperature;
        this.chargeCurrent = chargeCurrent;
        this.health = health;
        this.status = status;
        this.plugged = plugged;
    }

    public long getCurrentMillis() {
        return currentMillis;
    }

    public void setCurrentMillis(long currentMillis) {
        this.currentMillis = currentMillis;
    }

    public String getVoltage() {
        return voltage;
    }

    public void setVoltage(String voltage) {
        this.voltage = voltage;
    }


    public String getChargeCurrent() {
        return chargeCurrent;
    }

    public void setChargeCurrent(String chargeCurrent) {
        this.chargeCurrent = chargeCurrent;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlugged() {
        return plugged;
    }

    public void setPlugged(String plugged) {
        this.plugged = plugged;
    }
}
