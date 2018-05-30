package com.ckt.testauxiliarytool.tp.recorder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.tp.TPTestActivity;
import com.ckt.testauxiliarytool.tp.model.Constant;
import com.ckt.testauxiliarytool.utils.ActivityCollector;
import com.ckt.testauxiliarytool.utils.LogUtil;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;
import com.ckt.testauxiliarytool.utils.WinViewUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/16
 * TODO: 录屏服务
 */

public class ScreenRecordService extends Service {
    public static final String TAG = ScreenRecordService.class.getSimpleName();

    private NotificationManager mNotiManager;
    private RecordReceiver mRecordReceiver;

    /* 工作线程*/
    private HandlerThread mRecordWorkThread;
    private static Handler sRecordHandler;
    /* 计时器*/
    private TimerRunnable mTimerRunnable;

    /* 录屏引擎 */
    private ScreenRecordEngine mRecordEngine;

    /* 录屏浮窗 */
    private WinViewUtil mWinUtil;
    View.OnClickListener mWinListener = null;
    private View mWinView;

    private RecorderControllerImpl mRecorderController;
    /* 录屏通知id*/
    private int ID_NOTI_RECORD = 1;
    private RemoteViews mRemoteViews;

    private Notification mNoti;

    private List<IOnRecorderStateChangeCallback> mCallbacks;

    @Nullable
    @Override //多个Activity进行绑定，只会第一次绑定调用一次，其他的直接获取第一次绑定返回的值
    public IBinder onBind(Intent intent) {
        LogUtil.e(TAG, "service onBind");
        if (mRecorderController == null) {
            mRecorderController = new RecorderControllerImpl(this);
        }

        return mRecorderController;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化录屏准备工作
        initRecordWorks();
        // 初始化浮窗
        initWinToast();
        // 注册通知点击事件的广播
        registerRecordReceiver();
        // 初始化通知
        initNotification();
    }

    /**
     * 初始化录屏相关工作，工作线程、录屏引擎及回调等
     */
    private void initRecordWorks() {
        // 工作线程
        mRecordWorkThread = new HandlerThread("record_work_thread");
        mRecordWorkThread.start();
        sRecordHandler = new Handler(mRecordWorkThread.getLooper());

        mRecordEngine = new ScreenRecordEngine();
        mCallbacks = new ArrayList<>();
    }

    /**
     * 初始化录屏浮窗
     * <br/> 只有当应用处于后台时，才显示浮窗
     */
    private void initWinToast() {
        mWinView = LayoutInflater.from(this).inflate(R.layout.tp_layout_record_window_toast, null);
        mWinUtil = new WinViewUtil.Builder(this).setCustomView(mWinView).create();
        /* 点击计时浮窗后打开main活动 */
        mWinListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setClass(ScreenRecordService.this, TPTestActivity.class);
                startActivity(i);
            }
        };
        mWinUtil.getRootView().setOnClickListener(mWinListener);
    }


    /**
     * 初始化通知
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initNotification() {
        mNotiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /* 点击停止按钮 发送广播的意图 */
        Intent i = new Intent();
        i.setAction(Constant.ACTION_APP_STOP_RECORD);
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(ScreenRecordService.this, 1, i, PendingIntent.FLAG_CANCEL_CURRENT);

        /* 使用RemoteViews来布局 */
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.tp_item_view_remote_noti);
        mRemoteViews.setOnClickPendingIntent(R.id.id_btn_stop_noti, broadcastIntent); // 给停止按钮添加点击事件

        /* 点击通知文字打开main活动 */
        Intent intent = new Intent(ScreenRecordService.this, TPTestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(ScreenRecordService.this, 6, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.id_tv_title_noti, pi);
        mNoti = new Notification.Builder(this)
                .setContent(mRemoteViews)
                .setSmallIcon(R.mipmap.tp_ic_record_small)  // 记得设置icon，不然通知发送无效
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 如果返回 START_STICKY 是说服务应该一直运行除非我们手动停止它
        return START_STICKY;
    }

    /**
     * 开始录制
     */
    private void startRecord() {
        // 开始录屏
        mRecordEngine.startRecord();

        // 计时
        if (mTimerRunnable == null) mTimerRunnable = new TimerRunnable();
        mTimerRunnable.reset();
        sRecordHandler.post(mTimerRunnable);
        // 回调onRecordStart
        callOnRecordStart();
    }


    /**
     * 结束录制,释放资源
     *
     * @return 成功返回 true
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean stopRecord() {
        if (!mRecordEngine.stopRecord()) {
            callOnRecordStop(true);
            return false;
        }
        cancelRecordingNotification();
        mWinUtil.dismiss();
        // 回调onRecordStop
        callOnRecordStop(false);

        stopSelfSafely();

        return true;
    }

    private void stopSelfSafely() {
        if (isTPActivityDied()) {
            if (mRecorderController != null && !mRecorderController.isRecording()) {  // 没有录制则停止服务
                stopSelf();
            }
        }
    }

    private boolean isTPActivityDied() {
        for (Activity activity : ActivityCollector.getActivities()) {
            if (getString(R.string.tp_module_name).equals(activity.getTitle())) {
                return false;
            }
        }
        LogUtil.e(TAG, "TPActivityDied");
        return true;
    }


    /**
     * 调用所有注册的回调的onRecordStart方法
     */
    private void callOnRecordStart() {
        if (mCallbacks != null && mCallbacks.size() > 0) {
            for (IOnRecorderStateChangeCallback callback : mCallbacks) {
                callback.onRecordStart();
            }
        }
    }

    /**
     * 调用所有注册的回调的callOnRecordStop方法
     */
    private void callOnRecordStop(boolean error) {
        if (mCallbacks != null && mCallbacks.size() > 0) {
            for (IOnRecorderStateChangeCallback callback : mCallbacks) {
                callback.onRecordStop(error);
            }
        }
    }

    /**
     * 当开始录屏（{@link #startRecord()}被调用）的时候发送录屏通知
     * 在通知上可以点击停止录屏
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendRecordingNotification(String time) {
        if (mRemoteViews != null && mNoti != null && !TextUtils.isEmpty(time)) {
            mRemoteViews.setTextViewText(R.id.id_tv_title_noti, "正在录制:" + time);
            startForeground(ID_NOTI_RECORD, mNoti);
            mNotiManager.notify(ID_NOTI_RECORD, mNoti);
        }
    }

    /**
     * 取消录屏通知
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private void cancelRecordingNotification() {
        if (mNotiManager != null) {
            stopForeground(true);
            mNotiManager.cancel(ID_NOTI_RECORD);
        }
    }

    /**
     * 注册广播，用于录屏控制，前后台控制
     * <br/> 注意在恰当的时候取消注册,调用{@link #unregisterRecordReceiver()}
     */
    private void registerRecordReceiver() {
        mRecordReceiver = new RecordReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ACTION_APP_STOP_RECORD);
        filter.addAction(Constant.ACTION_APP_BACKGROUND);
        filter.addAction(Constant.ACTION_APP_FOREGROUND);
        registerReceiver(mRecordReceiver, filter);
    }

    /**
     * 取消注册广播
     */
    private void unregisterRecordReceiver() {
        if (mRecordReceiver != null) {
            unregisterReceiver(mRecordReceiver);
            mRecordReceiver = null;
        }
    }

    /**
     * 启动录屏服务
     * <p>
     * <br/> call this method in user-define Application
     *
     * @param context 上下文
     */
    public static void start(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) { // 大于5.0支持录屏
            // 启动服务
            context.startService(new Intent(context, ScreenRecordService.class));
        }
    }

    /**
     * 停止服务
     * @param context
     */
    public static void stop(Context context) {
        context.stopService(new Intent(context, ScreenRecordService.class));
    }


    /**
     * 处理控制录制,应用前后台的广播等
     */
    private class RecordReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Constant.ACTION_APP_STOP_RECORD: // 停止录屏
                    if (mRecordEngine.isRecording()) stopRecord();
                    break;
                case Constant.ACTION_APP_BACKGROUND: // 处于后台
                    if (mWinUtil != null && mRecordEngine.isRecording()) {
                        sRecordHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (Settings.canDrawOverlays(ScreenRecordService.this)) {
                                        mWinUtil.show();
                                    }
                                } else {
                                    mWinUtil.show();
                                }
                            }
                        });
                    }
                    break;
                case Constant.ACTION_APP_FOREGROUND: // 处于前台
                    if (mWinUtil != null) {
                        sRecordHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mWinUtil.isShowing())
                                    mWinUtil.dismiss();
                            }
                        });
                    }
                    break;
            }
        }
    }

    /**
     * 计时与通知发送工作
     */
    private class TimerRunnable implements Runnable {
        private int counter = 0;

        @Override
        public void run() {
            if (mRecordEngine.isRecording()) {
                String time = getStringTime(counter);
                ++counter;
                // 发通知
                sendRecordingNotification(time);
                // 回调录屏更新
                if (mCallbacks != null && mCallbacks.size() > 0) {
                    for (IOnRecorderStateChangeCallback callback : mCallbacks) {
                        callback.onRecordUpdate(time);
                    }
                }
                // 设置浮窗文字
                mWinUtil.setTextViewText(mWinView, R.id.id_tv_record_win_toast, time);
                sRecordHandler.postDelayed(this, 1000);  // 每一秒刷新下通知
            }
        }

        public void reset() {
            counter = 0;
        }
    }

    /**
     * 将数字转化为时间显示
     *
     * @param count 秒数
     * @return 时间字符串表示
     */
    private String getStringTime(int count) {
        int hour = count / 3600;
        int min = count % 3600 / 60;
        int second = count % 60;
        return String.format(Locale.CHINA, "%02d:%02d:%02d", hour, min, second);
    }

    /**
     * 添加状态改变回调
     *
     * @param callback {@link IOnRecorderStateChangeCallback}
     */
    void addOnRecorderStateChangeCallback(IOnRecorderStateChangeCallback callback) {
        if (callback != null && !mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    /**
     * 移除回调
     *
     * @param callback {@link IOnRecorderStateChangeCallback}
     */
    public void removeRecordingCallback(IOnRecorderStateChangeCallback callback) {
        if (mCallbacks.contains(callback)) {
            mCallbacks.remove(callback);
            LogUtil.e(ScreenRecordService.class.getName(), "removeRecordingCallback:" + callback.getClass().getName());
        }
    }

    /**
     * 移除所有回调
     */
    private void removeAllRecordingCallbacks() {
        if (mCallbacks == null) return;
        for (IOnRecorderStateChangeCallback callback : mCallbacks) {
            mCallbacks.remove(callback);
        }
    }

    /**
     * 录制控制器
     */
    private class RecorderControllerImpl extends Binder implements IRecorderController {
        private ScreenRecordService service;

        public RecorderControllerImpl(ScreenRecordService service) {
            this.service = service;
        }

        @Override
        public void startRecord() {
            sRecordHandler.post(new Runnable() {
                @Override
                public void run() {
                    service.startRecord();
                }
            });
        }

        @Override
        public boolean stopRecord() {
            return service.stopRecord();
        }

        @Override
        public void setConfig(int width, int height, int dpi) {
            mRecordEngine.setConfig(width, height, dpi);
        }

        @Override
        public boolean isRecording() {
            return mRecordEngine.isRecording();
        }

        @Override
        public void setMediaProject(MediaProjection projection) {
            mRecordEngine.setMediaProjection(projection);
        }

        @Override
        public void addRecordingCallback(IOnRecorderStateChangeCallback callback) {
            service.addOnRecorderStateChangeCallback(callback);
        }

        @Override
        public void removeRecordingCallback(IOnRecorderStateChangeCallback callback) {
            service.removeRecordingCallback(callback);
        }

        public void removeAllRecordingCallbacks() {
            service.removeAllRecordingCallbacks();
        }

        @Override
        public String getStoreFileAbsolutePath() {
            return mRecordEngine.getAbsoluteFilePath();
        }

        @Override
        public String getStoreFileDir() {
            return mRecordEngine.getSavedFileDir();
        }

        public void setService(ScreenRecordService service) {
            this.service = service;
        }

    }

    /**
     * 获取正在运行的服务个数
     *
     * @return
     */
    private int getRunningServiceCount() {
        int count = 0;
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getClassName() != null && service.service.getClassName().startsWith("com.ckt.testauxiliarytool")) {
                LogUtil.e(TAG, service.service.getClassName());
                ++count;
            }
        }
        return count;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 释放录屏资源
        if (mRecordEngine != null) {
            if (mRecordEngine.isRecording()) stopRecord();
            mRecordEngine.releaseRecorder();
            mRecordEngine = null;
        }

        // 移除控制回调
        if (mRecorderController != null) {
            mRecorderController.removeAllRecordingCallbacks();
            mRecorderController.setService(null);
            mRecorderController = null;
        }
        // 释放Handler资源
        if (sRecordHandler != null) {
            sRecordHandler.removeCallbacksAndMessages(null);
            mTimerRunnable = null;
            sRecordHandler = null;
        }

        // 释放工作线程资源
        if (mRecordWorkThread != null && mRecordWorkThread.isAlive()) {
            mRecordWorkThread.quitSafely();
            mRecordWorkThread.interrupt();
            mRecordWorkThread = null;
        }

        // 释放浮窗资源
        if (mWinUtil != null) {
            mWinUtil.clear();
            mWinUtil = null;
            mWinListener = null;
        }

        // 取消录屏广播注册
        unregisterRecordReceiver();

        // 移除RemoteViews的视图
        if (mRemoteViews != null) {
            mRemoteViews.removeAllViews(R.layout.tp_item_view_remote_noti);
            mRemoteViews = null;
        }

        // 取消所有消息
        if (mNotiManager != null) {
            mNotiManager.cancelAll();
            mNotiManager = null;
            mNoti = null;
        }

        LogUtil.e(TAG, "service onDestroy");

        // 无其他运行的常驻服务，并且Activity都已经被销毁
        if (getRunningServiceCount() <= 1 && ActivityCollector.getActivities().size() <= 0) {
            // 释放其他资源
            SharedPrefsUtil.release();
            LogUtil.e(TAG, "app closed completely");
            // 如果服务停止，则彻底停止应用
            Process.killProcess(Process.myPid());
        }

    }
}
