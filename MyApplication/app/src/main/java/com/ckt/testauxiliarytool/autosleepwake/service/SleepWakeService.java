package com.ckt.testauxiliarytool.autosleepwake.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.ckt.testauxiliarytool.autosleepwake.interfaces.Constants;
import com.ckt.testauxiliarytool.utils.FileUtils;
import com.ckt.testauxiliarytool.utils.GetRamRomSdUtil;
import com.ckt.testauxiliarytool.utils.LogUtil;
import com.ckt.testauxiliarytool.utils.TestUtil;

import java.io.File;
import java.lang.ref.WeakReference;

public class SleepWakeService extends Service {
    public static final String TAG = SleepWakeService.class.getSimpleName();
    // 该标记用于前台UI显示，同步
    private static boolean isRunning = false;
    // 用于停止测试线程
    private static boolean isShouldStop = false;
    // 当前已经测完的次数
    public static int sFinishCount = 0;

    private static OnTestTaskListener sOnTestTaskListener;

    /**
     * 测试任务监听器，测试运行时回调onTaskRunning()
     * 测试完成时回调onTaskStop()，用于更新前台UI显示
     */
    public interface OnTestTaskListener {

        void onTaskRunning();

        void onTaskStop();

    }

    public static void setOnTaskTestListener(OnTestTaskListener onTestTaskListener) {
        sOnTestTaskListener = onTestTaskListener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand");
        new TestThread(intent, this).start();
        if (sOnTestTaskListener != null) {
            sOnTestTaskListener.onTaskRunning();
        }
        isRunning = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (sOnTestTaskListener != null) {
            sOnTestTaskListener.onTaskStop();
        }
        LogUtil.d(TAG, "onDestroy");
    }

    /**
     * 暴露给外部调用，启动服务
     *
     * @param context 上下文
     * @param time    测试时间间隔
     * @param count   测试次数
     */
    public static void start(Context context, int time, int count) {
        Intent intent = new Intent(context, SleepWakeService.class);
        intent.putExtra(Constants.TEST_TIME, time);
        intent.putExtra(Constants.TEST_COUNT, count);
        context.startService(intent);
    }

    /**
     * 暴露给外部调用，停止测试服务
     *
     * @param context 上下文
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, SleepWakeService.class);
        isShouldStop = true;
        context.stopService(intent);
    }

    public static boolean getRunningStatus() {
        return isRunning;
    }

    /**
     * 执行测试的子线程
     */
    private static class TestThread extends Thread {
        private Intent mIntent;
        private WeakReference<SleepWakeService> mSleepWakeServiceRef;
//        private volatile boolean isShouldStop = false;

        TestThread(Intent intent, SleepWakeService sleepWakeService) {
            this.mIntent = intent;
            mSleepWakeServiceRef = new WeakReference<>(sleepWakeService);
        }

        @Override
        public void run() {
            //从Intent中取出测试间隔和测试次数
            int time = mIntent.getIntExtra(Constants.TEST_TIME, 0);
            int millisecond = time * 1000; // 转换为毫秒
            int count = mIntent.getIntExtra(Constants.TEST_COUNT, 0);
            SleepWakeService sleepWakeService = mSleepWakeServiceRef.get();
            if (sleepWakeService != null) {
                PowerManager powerManager = (PowerManager) sleepWakeService.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ServiceWakeLock");
                wakeLock.acquire();
                String logPath; // 打印日志的文件路径
                if (GetRamRomSdUtil.externalMemoryAvailable()) {
                    LogUtil.d(TAG, "内置SD卡存在");
                    if (FileUtils.createOrExistsDir(Constants.LOG_DIR)) {
                        LogUtil.d(TAG, "CKTAutoTest目录存在或创建成功");
                        String fileName = TestUtil.millis2String(System.currentTimeMillis()).replace(" ", "_");
                        logPath = Constants.LOG_DIR + File.separator + fileName + ".log";
                        if (FileUtils.createOrExistsFile(logPath)) {
                            LogUtil.d(TAG, "日志文件创建成功");
                            for (int i = 1; i <= count; i++) {
                                SystemClock.sleep(millisecond);
                                if (isShouldStop) {
                                    wakeLock.release();
                                    return;
                                }
                                TestUtil.lock(sleepWakeService, logPath, i);
                                SystemClock.sleep(millisecond);
                                sFinishCount = i;
                                TestUtil.wakeUp(sleepWakeService, logPath, i);
                            }
                            wakeLock.release();
                            sleepWakeService.stopSelf();
                        }
                    }
                }
            }
        }
    }
}