package com.ckt.testauxiliarytool.sensortest.bean;

/**
 * Created by D22432 on 2017/9/8.
 * times record the times (in Album and Player)or angles (in Gyroscope).
 * time record the time,Units are milliseconds.
 */
public class CommendResult {
    private int times;
    private int time;

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "AngleTime{" +
                "times=" + times +
                ", time=" + time +
                '}';
    }
}