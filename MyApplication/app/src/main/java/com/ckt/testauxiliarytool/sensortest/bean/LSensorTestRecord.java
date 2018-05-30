package com.ckt.testauxiliarytool.sensortest.bean;

/**
 * Created by D22400 on 2017/11/2.
 * Email:danfeng.qiu@ck-telecom.com
 * Describe:
 */

public class LSensorTestRecord {

    private String mTestName;
    private float mLux;
    private String mRange;
    private long mTime;

    public String getTestName() {
        return mTestName;
    }

    public void setTestName(String testName) {
        mTestName = testName;
    }

    public float getLux() {
        return mLux;
    }

    public void setLux(float lux) {
        mLux = lux;
    }

    public String getRange() {
        return mRange;
    }

    public void setRange(String range) {
        mRange = range;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }
}
