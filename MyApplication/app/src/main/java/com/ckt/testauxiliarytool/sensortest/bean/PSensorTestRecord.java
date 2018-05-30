package com.ckt.testauxiliarytool.sensortest.bean;

/**
 * Created by D22400 on 2017/10/23.
 * Email:danfeng.qiu@ck-telecom.com
 * Describe:
 */

public class PSensorTestRecord {
    private int mScreenOffTime;
    private int mScreenOnTime;

    public PSensorTestRecord(int screenOffTime) {
        mScreenOffTime = screenOffTime;
    }

    public int getScreenOffTime() {
        return mScreenOffTime;
    }

    public void setScreenOffTime(int screenOffTime) {
        mScreenOffTime = screenOffTime;
    }

    public int getScreenOnTime() {
        return mScreenOnTime;
    }

    public void setScreenOnTime(int screenOnTime) {
        mScreenOnTime = screenOnTime;
    }
}
