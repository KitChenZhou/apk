package com.ckt.testauxiliarytool.cameratest.capturetask;

/**
 * Created by D22433 on 2017/9/4.
 */
public class Sleeper {
    public static void sleep(int cnt){
        try {
            Thread.sleep(cnt * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
