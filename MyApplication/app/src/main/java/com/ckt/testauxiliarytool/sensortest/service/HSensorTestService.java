package com.ckt.testauxiliarytool.sensortest.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.sensortest.LcdBrightnessGetterForHall;
import com.ckt.testauxiliarytool.sensortest.activities.PSensorTestActivity;
import com.ckt.testauxiliarytool.sensortest.bean.HSensor;
import com.ckt.testauxiliarytool.sensortest.db.SensorLab;

import static com.ckt.testauxiliarytool.sensortest.SensorType.TYPE_HSENSOR_CALL;

/**
 * Created by D22434 on 2017/9/15.
 */

public class HSensorTestService extends Service implements LcdBrightnessGetterForHall.BrightnessListener {
    private static final String TAG = "HSensorTestService";
    private static String ACTION_CLEAR_COVER_STATE_CHANGE = "android.intent.action.HALL_STATE";

    private int NOTIFICATION_ID = 1;// 如果id设置为0,会导致不能设置为前台service
    private final int DARK = 0;
    private final int LIGHT = 1;

    private int mStatus;
    private long mStartTime = -1;
    private boolean mComplete_dark = false;

    //创建一个内部类的实例
    private MyBinder myBinder = new MyBinder();
    private SensorLab mSensorLab;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private HallReceiver mBroadcastReceiver;
    private LcdBrightnessGetterForHall mLcdBrightnessGetterForHall;

    @Override
    public void onCreate() {
        super.onCreate();

        //获取实例化
        mSensorLab = SensorLab.get(this);
        mLcdBrightnessGetterForHall = new LcdBrightnessGetterForHall(this);
        mLcdBrightnessGetterForHall.startListen(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CLEAR_COVER_STATE_CHANGE);

        mBroadcastReceiver = new HallReceiver();
        registerReceiver(mBroadcastReceiver, intentFilter);

    }

    /**
     * 创建前台notification 服务
     */
    private void showNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getClass().getSimpleName())
                .setContentText("HallSensor 电话模式测试")
                .build();

        if (mNotificationManager != null) {
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            startForeground(NOTIFICATION_ID, mNotification);
        }
    }

    private void cancelNotification() {
        if (mNotificationManager != null) {
            stopForeground(true);
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //绑定的同时，注册PSensor监听器并且发送通知告知测试已开始。
        showNotification();
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    //定义一个类去继承Binder
    public class MyBinder extends Binder {
        //内部类里定义一个公有方法
        public HSensorTestService getService() {
            return HSensorTestService.this;//将当前服务的实例返回
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelNotification();
        unregisterReceiver(mBroadcastReceiver);
        if (mLcdBrightnessGetterForHall.isListening()) {
            mLcdBrightnessGetterForHall.stopListen();
        }
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void afterGetLcdBrightness(String brightness) {
        if (brightness.equals("0") && mStatus == DARK) {
            Log.i(PSensorTestActivity.TAG, "灭屏时的开始时间: " + mStartTime);
            long duration = System.currentTimeMillis() - mStartTime;
            showRecord(duration, mStatus);
            Log.e(TAG, "灭屏=" + duration);
            mComplete_dark = true;
        } else if (mComplete_dark && !brightness.equals("0") && mStatus == LIGHT) {
            mComplete_dark = false;
            Log.i(PSensorTestActivity.TAG, "亮屏时的开始时间: " + mStartTime);
            long duration = System.currentTimeMillis() - mStartTime;
            showRecord(duration, mStatus);
            Log.e(TAG, "亮屏=" + duration);
        }
    }

    /**
     * hall sensor 广播，监听
     */
    class HallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, intent.getAction() + "--");
            if (intent.getAction().equals(ACTION_CLEAR_COVER_STATE_CHANGE)) {
                mStartTime = System.currentTimeMillis();
                if (intent.getIntExtra("state", 0) == 0) {
                    mStatus = DARK;
                } else {
                    mStatus = LIGHT;
                }
                Log.d(TAG, "ACTION:COVER_STATE_CHANGE：" + intent.getIntExtra("state", 0));
            }
        }
    }

    /**
     * 保存记录
     *
     * @param time
     * @param status
     */
    private void showRecord(long time, int status) {
        if (status == 0) {
            mSensorLab.addHRecord(new HSensor(TYPE_HSENSOR_CALL, "合盖/灭屏", time));
        } else {
            mSensorLab.addHRecord(new HSensor(TYPE_HSENSOR_CALL, "开盖/亮屏", time));
        }
    }


}