package com.ckt.testauxiliarytool.sensortest;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by D22400 on 2017/9/7.
 * LCD屏幕亮度值获取类
 */

public class LcdBrightnessGetterForHall {


    private final String SYS_PATH = "/sys/class/leds/lcd-backlight/brightness";

    private final String TAG = "LCD_GETTER.";

    private boolean mListenFlag = false;
    private boolean mLSensorTestStartFlag = false;
    private String mCurrentBrightness;
    private long mTempTime;

    private Context mContext;
    private ExecutorService mThreadPool;

    public LcdBrightnessGetterForHall(Context context) {
        mContext = context;
        mThreadPool = Executors.newCachedThreadPool();
    }



    private void listenForLSensorTest(final LSensorTestBrightnessListener listener) {
        String prop;// 默认值
        BufferedReader br = null;
        mCurrentBrightness = "";
        mTempTime = -1;
        try {
            while (mListenFlag) {
                br = new BufferedReader(new FileReader(SYS_PATH));
                prop = br.readLine();
                if (!mLSensorTestStartFlag) {
                    //该if语句用来在测试中途停止时重置Temp时间以防时间间隔超过3000毫秒的异常
                    mTempTime = -1;
                }
                if (!prop.equals(mCurrentBrightness)) {
                    if (mLSensorTestStartFlag) {
                        mTempTime = System.currentTimeMillis();
                    }
                    mCurrentBrightness = prop;
                    Log.i(TAG, "LCD亮度值: " + prop);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.afterGetLcdBrightness(mCurrentBrightness);
                        }
                    });
                } else {
                    //不触发监听器，就无法使mStartFlag为true，就无法产生mStopTime
                    if (mTempTime != -1 && mLSensorTestStartFlag
                            && (System.currentTimeMillis() - mTempTime) >= 3000) {
                        Log.i(TAG, "结束时间: " + mTempTime);
                        mLSensorTestStartFlag = false;
                        final long stopTime = mTempTime;
                        mTempTime = -1;
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.stopChange(stopTime, mCurrentBrightness);
                            }
                        });
                    }
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, " ***ERROR*** : " + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void listenBrightness(BrightnessListener listener) {
        mCurrentBrightness = "";
        BufferedReader br = null;
        try {
            while (mListenFlag) {
                String prop;
                br = new BufferedReader(new FileReader(SYS_PATH));
                prop = br.readLine();
                if (!prop.equals(mCurrentBrightness)) {
                    mCurrentBrightness = prop;
                    Log.i(TAG, "read data ---> " + mCurrentBrightness);
                    listener.afterGetLcdBrightness(mCurrentBrightness);
                }
                br.close();
            }
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
    }

    public void startListen(final BrightnessListener listener) {
        if (isListening()) {
            return;
        }
        mListenFlag = true;
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                listenBrightness(listener);
            }
        });
    }

    /**
     * 为L-SENSOR使用的开始监听LCD亮度值的方法
     *
     * @param listener 监听器，含回调方法
     */
    public void startListenForLSensorTest(final LSensorTestBrightnessListener listener) {
        if (isListening()) {
            return;
        }
        mListenFlag = true;
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                listenForLSensorTest(listener);
            }
        });
    }

    public void stopListen() {
        mListenFlag = false;
    }

    public void startLSensorTest() {
        mLSensorTestStartFlag = true;
    }

    public void stopLSensorTest() {
        mLSensorTestStartFlag = false;
    }

    public boolean isListening() {
        return mListenFlag;
    }

    public interface LSensorTestBrightnessListener {
        void afterGetLcdBrightness(String brightness);

        void stopChange(long stopTime, String stopBrightness);
    }

    public interface BrightnessListener {
        void afterGetLcdBrightness(String brightness);
    }

}
