package com.ckt.testauxiliarytool.batterymonitor.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.utils.LogUtil;

/**
 * 监控电池变化的服务，该服务仅起到监控的作用，但一定要保证该服务不被杀死
 */
public class BatteryMonitorService extends Service {
    public static final String TAG = BatteryMonitorService.class.getSimpleName();
    private BatteryChangeReceiver mBatteryChangeReceiver;
    private int mLastLevel = -1; // 保存上一次的电量
    private NotificationManagerCompat mNotificationManager;
    private static boolean isRunning = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG,"BatteryMonitorService is onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG,"BatteryMonitorService is onStartCommand");
        isRunning = true;
        registerReceiver(mBatteryChangeReceiver = new BatteryChangeReceiver(),
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        mNotificationManager = NotificationManagerCompat.from(this);
        return START_STICKY;
    }

    /**
     * 开启服务
     *
     * @param context 上下文
     */
    public static void start(Context context) {
        if (isRunning) return;
        Intent intent = new Intent(context, BatteryMonitorService.class);
        context.startService(intent);
    }

    /**
     * 停止服务
     *
     * @param context 上下文
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, BatteryMonitorService.class);
        context.stopService(intent);
    }

    /**
     * 获取服务的运行状态，也可利用ActivityManager方式来获取
     *
     * @return 如果服务在运行中，则返回true，否则返回false
     */
    public static boolean getRunningStatus() {
        return isRunning;
    }

    /**
     * 发送电量改变的通知，根据电量值来显示对应的小图片
     *
     * @param level 电量
     */
    private void sendLevelNotification(int level) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        TypedArray iconIds = getResources().obtainTypedArray(R.array.levelIcons);
        builder.setSmallIcon(iconIds.getResourceId(level, R.drawable.level_000));
        iconIds.recycle();
        builder.setContentTitle("BatteryMonitor")
                .setContentText("电池监测服务已开启");
        mNotificationManager.notify(0, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        unregisterReceiver(mBatteryChangeReceiver);
        mNotificationManager.cancelAll();
        LogUtil.d(TAG,"BatteryMonitorService is Destroy!");
    }

    /**
     * 电池状态改变的广播接收器，电量每变化1%，记录一次数据
     */
    private class BatteryChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                LogUtil.d(TAG,"BatteryChangeReceiver onReceive");
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0); // 电量
                if (level != mLastLevel) { // 只有电量变化时才需要记录数据
                    RecordIntentService.start(context, intent); // 开启记录Service，向数据库写数据
                    mLastLevel = level;
                    sendLevelNotification(level);
                }
            }
        }
    }
}
