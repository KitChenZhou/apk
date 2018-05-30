package com.ckt.testauxiliarytool;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;

import com.ckt.testauxiliarytool.tp.adapters.ActivityLifecycleCallbacksAdapter;
import com.ckt.testauxiliarytool.tp.model.Constant;
import com.ckt.testauxiliarytool.utils.ActivityCollector;

/**
 * Date: 2017/8/18.
 * TODO: 自定义Application
 */

public class MyApplication extends Application {
    private static Context sContext;
    private static int sCount = 0;

    private static Handler mMainHandler;


    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {

        mMainHandler = new Handler(getMainLooper());
        sContext = this;

        // 注册Activity生命周期回调,用于判断应用前后台状态
        registerActivityLifecycleCallbacks();
    }

    /**
     * 注册Activity生命周期回调,用于判断应用前后台状态
     */
    private void registerActivityLifecycleCallbacks() {
        ActivityLifecycleCallbacksAdapter lifecycleCallback = new ActivityLifecycleCallbacksAdapter() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // 添加新Activity
                ActivityCollector.addActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                super.onActivityStarted(activity);
                ++sCount;
                sendAppStateBroadcast();
            }

            @Override
            public void onActivityStopped(Activity activity) {
                super.onActivityStopped(activity);
                --sCount;
                sendAppStateBroadcast();
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                super.onActivityDestroyed(activity);
                ActivityCollector.removeActivity(activity);
            }
        };
        registerActivityLifecycleCallbacks(lifecycleCallback);
    }

    /**
     * 发送应用处于前后台状态的广播
     */
    private void sendAppStateBroadcast() {
        if (sCount <= 0) {
            Intent intent = new Intent(Constant.ACTION_APP_BACKGROUND);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent(Constant.ACTION_APP_FOREGROUND);
            sendBroadcast(intent);
        }
    }


    /**
     * 获取全局上下文
     *
     * @return {@link Context}
     */
    public static Context getContext() {
        return sContext;
    }

    /**
     * 获取运行的活动
     *
     * @return
     */
    public static int getAliveCount() {
        return sCount;
    }

    /**
     * app是否处于后台
     *
     * @return
     */
    public static boolean isAppBackground() {
        return sCount <= 0;
    }

    public static Resources getGlobalResources() {
        return sContext.getResources();
    }

    public static Handler getMainHander() {
        return mMainHandler;
    }

    public static Thread getMainThread() {
        return Thread.currentThread();
    }

    public static long getMainThreadId() {
        return Thread.currentThread().getId();
    }

}
