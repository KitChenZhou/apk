package com.ckt.testauxiliarytool.sensortest;

import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by D22400 on 2017/10/20.
 * Email:danfeng.qiu@ck-telecom.com
 * Describe:获取LCD的亮度值的工具类
 */

public class LcdBrightnessGetter {

    private static final String TAG = "LCD_GETTER";
    private static final String PROP_PATH = "/sys/class/leds/lcd-backlight/brightness";

    /**
     * --静态方法--
     * 获取LCD亮度值
     * 该方法含IO操作，应在线程中使用！
     *
     * @return 从节点中读取到的LCD亮度值
     */
    public static int getLcdBrightness() throws Exception {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //判断是否在主线程中运行，若是，则抛出异常
            throw new Exception("getLcdBrightness()应在线程中使用");
        }
        String brightness = "-1";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(PROP_PATH));
            brightness = br.readLine();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "getLcdBrightness: ");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Integer.parseInt(brightness);
    }
}