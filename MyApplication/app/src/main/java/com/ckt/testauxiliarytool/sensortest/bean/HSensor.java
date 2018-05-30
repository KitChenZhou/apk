package com.ckt.testauxiliarytool.sensortest.bean;

/**
 * Created by D22434 on 2017/8/22.
 * MSensor和HSensor的数据对象
 */

public class HSensor {
    private int type;
    private String status;
    private long interval;

    public HSensor(int type, String status, long interval) {
        this.type = type;
        this.status = status;
        this.interval = interval;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }
}
