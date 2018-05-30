package com.ckt.testauxiliarytool.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ckt.testauxiliarytool.MyApplication;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/25
 * TODO:UI相关的工具
 */

public class UiUtil {


    private static Toast sToast;

    /**
     * 更新全屏状态
     *
     * @param isFullscreen 是否全屏
     */
    public static void updateFullscreenStatus(AppCompatActivity context, boolean isFullscreen) {
        if (isFullscreen) {
            context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            if (context.getSupportActionBar() != null) {
                context.getSupportActionBar().hide();
            }
            hideNavigationBar(context);
        } else {
            context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (context.getSupportActionBar() != null) {
                context.getSupportActionBar().show();
            }
            showNavigationBar(context);
        }
        context.getWindow().getDecorView().requestLayout();
    }

    /**
     * 隐藏导航栏
     * @param activity {@link Activity}
     *<br/>FLAG:
     *<br/>SYSTEM_UI_FLAG_VISIBLE ——显示状态栏和导航栏
     *<br/>SYSTEM_UI_FLAG_LOW_PROFILE——此模式下，状态栏的图标可能是暗的
     *<br/>SYSTEM_UI_FLAG_HIDE_NAVIGATION——隐藏导航栏
     *<br/>SYSTEM_UI_FLAG_FULLSCREEN——全屏，隐藏状态栏和导航栏
     *<br/>SYSTEM_UI_FLAG_LAYOUT_STABLE
     *<br/>SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
     *<br/>SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN——全屏，隐藏导航栏，状态栏浮在布局上。
     *<br/>SYSTEM_UI_FLAG_IMMERSIVE——沉浸式：半透明的状态栏和导航栏
     *<br/>SYSTEM_UI_FLAG_IMMERSIVE_STICKY——粘性沉浸式
     */
    public static void hideNavigationBar(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_IMMERSIVE  // 沉浸式，导航栏需上划才出现
               ;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public static void showNavigationBar(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
    }


    /** 主线程*/
    public static Thread getMainThread() {
        return MyApplication.getMainThread();
    }

    /** 主线程id*/
    public static long getMainThreadId() {
        return MyApplication.getMainThreadId();
    }

    /** 判断当前的线程是不是在主线程 */
    public static boolean isRunInMainThread() {
        return android.os.Process.myTid() == getMainThreadId();
    }

    /**
     * 主线程执行任务
     * @param runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        if (isRunInMainThread()) {
            runnable.run();
        } else {
            post(runnable);
        }
    }

    /**
     * 对toast的简易封装。线程安全，可以在非UI线程调用。
     */
    public static void showToastSafe(final Context context, final String str) {
        if (isRunInMainThread()) {
            showToast(context, str);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    showToast(context, str);
                }
            });
        }
    }


    private static void showToast(Context context, String str) {
        if (sToast == null) {
            sToast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        } else {
            sToast.setText(str);
            sToast.setDuration(Toast.LENGTH_SHORT);
        }
        sToast.show();
    }
    /**
     * 获取资源
     */
    public static Resources getResources() {
        return MyApplication.getGlobalResources();
    }

    /**
     * 获取dimen
     */
    public static int getDimens(int resId) {
        return getResources().getDimensionPixelSize(resId);
    }

    /**
     * 获取drawable
     */
    public static Drawable getDrawable(int resId) {
        return getResources().getDrawable(resId);
    }

    /**
     * 获取文字
     */
    public static String getString(Context context, int resId) {
        return getResources().getString(resId);
    }

    public static String getString(int resId) {
        return MyApplication.getGlobalResources().getString(resId);
    }



    /**
     * 获取颜色
     */
    public static int getColor(int resId) {
        return getResources().getColor(resId);
    }

    public static int getColor(Context context,int resId) {
        return context.getResources().getColor(resId);
    }


    /**
     * 获取主线程的handler
     */
    public static Handler getMainHandler() {
        return MyApplication.getMainHander();
    }

    /**
     * 延时在主线程执行runnable
     */
    public static boolean postDelayed(Runnable runnable, long delayMillis) {
        return getMainHandler().postDelayed(runnable, delayMillis);
    }

    /**
     * 在主线程执行runnable
     */
    public static boolean post(Runnable runnable) {
        return getMainHandler().post(runnable);
    }

    /**
     * 从主线程looper里面移除runnable
     */
    public static void removeCallbacks(Runnable runnable) {
        getMainHandler().removeCallbacks(runnable);
    }


    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static String getVersion(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionName;
    }
}
