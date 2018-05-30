package com.ckt.testauxiliarytool.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.ckt.testauxiliarytool.MyApplication;
import com.ckt.testauxiliarytool.tp.model.ScreenInfo;
import com.ckt.testauxiliarytool.tp.model.ScreenSize;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/21.
 * TODO: 尺寸工具
 */

public class SizeUtil {
    //转换dp为px
    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, MyApplication.getGlobalResources().getDisplayMetrics());
    }


    //转换sp为px
    public static int sp2px(Context context, int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, MyApplication.getGlobalResources().getDisplayMetrics());
    }

    /**
     * px -> mm
     * <br/>1 inch 英寸=25.4 millimetres 毫米
     *<br/> metrics.xdpi 是屏幕物理尺寸密度，每英寸inch有多少像素pixels，pixels per inch
     *<br/>px =mm *metrics.xdpi *(1.0f/25.4f)
     *<br/>mm=px/(metrics.xdpi *(1.0f/25.4f))
     *
     * */
    public static float px2mm_x(int px) {
        DisplayMetrics metrics = MyApplication.getGlobalResources().getDisplayMetrics();
        float mm = px / (metrics.xdpi * (1.0f / 25.4f));
        return mm;
    }

    /**
     * @see #px2mm_x(int)
     * */
    public static float px2mm_y(int px) {
        DisplayMetrics metrics = MyApplication.getGlobalResources().getDisplayMetrics();
        float mm = px / (metrics.ydpi * (1.0f / 25.4f));
        return mm;
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight() {
        Resources resources = MyApplication.getGlobalResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        Log.v("TAG", "Status height:" + height);
        return height;
    }

    /**
     * 获取虚拟功能键高度
     */
    public static int getNavigationBarHeight() {
        int height = 0;
        WindowManager windowManager = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        try {
            Class c = Class.forName("android.view.Display");
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, outMetrics);
            height = outMetrics.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return height;
    }

    /**
     * 获取屏幕物理尺寸(英寸)
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static double getScreenInch() {
        Point outSize = new Point();
        WindowManager wm = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealSize(outSize);
        DisplayMetrics metrics = MyApplication.getContext().getResources().getDisplayMetrics();
        double x = Math.pow(outSize.x / metrics.xdpi, 2);
        double y = Math.pow(outSize.y / metrics.ydpi, 2);
        return Math.sqrt(x + y);
    }


    /**
     * 获取屏幕真实宽高尺寸 (px)
     * @param outSize {@link ScreenSize}
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void getRealScreenSize(ScreenSize outSize) {
        WindowManager wm = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Point out = new Point();
        wm.getDefaultDisplay().getRealSize(out);
        outSize.screenWidth = out.x;
        outSize.screenHeight = out.y;
    }

    /**
     *获取屏幕真实宽高尺寸 (px)
     *
     * @param outSize {@link ScreenSize}
     */

    public static void getScreenSize3(ScreenSize outSize) {
        WindowManager wm = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        outSize.screenWidth = wm.getDefaultDisplay().getWidth();
        outSize.screenHeight = wm.getDefaultDisplay().getHeight();
    }


    /**
     *获取屏幕尺寸(px,受导航栏，标题栏影响)
     *
     * @param outSize {@link ScreenSize}
     */
    public static void getScreenSize(ScreenSize outSize) {
        WindowManager wm = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        outSize.screenWidth = outMetrics.widthPixels;
        outSize.screenHeight = outMetrics.heightPixels;

    }

    /**
     *获取屏幕真实尺寸(px,受导航栏，标题栏影响)
     *
     * @param outSize {@link ScreenSize}
     */
    public static void getScreenSize2(ScreenSize outSize) {
        Resources resources = MyApplication.getGlobalResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density1 = dm.density;
        outSize.screenWidth = dm.widthPixels;
        outSize.screenHeight = dm.heightPixels;

    }

    /**
     * 获取屏幕信息
     *
     * @param screenInfo {@link ScreenInfo}
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void getScreenInfo(ScreenInfo screenInfo) {
        if (screenInfo == null) {
            throw new NullPointerException("screenInfo should not be null");
        }

        WindowManager wm = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics outMetrics = new DisplayMetrics();
//        wm.getDefaultDisplay().getMetrics(outMetrics);
        Resources resources = MyApplication.getGlobalResources();
        DisplayMetrics outMetrics = resources.getDisplayMetrics();
        Point out = new Point();
        wm.getDefaultDisplay().getRealSize(out);

        screenInfo.widthPixels = out.x;
        screenInfo.heightPixels = out.y;
        screenInfo.densityDpi = outMetrics.densityDpi;
        screenInfo.density = outMetrics.density;
        screenInfo.xdpi = outMetrics.xdpi;
        screenInfo.ydpi = outMetrics.ydpi;
        screenInfo.screenInche = getScreenInch();
        screenInfo.realWidth = px2mm_x(screenInfo.widthPixels);
        screenInfo.realHeight = px2mm_y(screenInfo.heightPixels);
    }

    /**
     * list<string> 转 string[]
     *
     * @param list {@link List}
     * @return  String[]
     */
    public static String[] list2Array(List<String> list) {
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }


    /**
     * list转string，以 ，分隔
     *
     * @param list List<String>
     * @return String
     */
    public static String list2String(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size()) {
                sb.append(",");
            }
        }

        return sb.toString();
    }
}
