package com.ckt.testauxiliarytool.sensortest.bean;

/**
 * Created by D22434 on 2017/8/22.
 * MSensor的数据对象
 */

public class MSensor {
    private int type;
    private int angle;
    private float deviation;

    public MSensor(int type, int angle, float deviation) {
        this.type = type;
        this.angle = angle;
        this.deviation = deviation;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public float getDeviation() {
        return deviation;
    }

    public void setDeviation(float deviation) {
        this.deviation = deviation;
    }
}
