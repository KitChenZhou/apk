package com.ckt.testauxiliarytool.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.MyApplication;
import com.ckt.testauxiliarytool.tp.model.Constant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/8.
 * TODO:日志工具，可设置level来控制日志的输出等级
 */

public class LogUtil {
    /* 日志级别，依次递增 */
    private static final int VERBOSE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;
    private static final int NOTHING = 6;

    public static int level = VERBOSE;  // 日志过滤级别

    public static final String PATH = Constant.CRASH_OUTPUT_FILE_DIR;
    private static final String FILE_NAME = "crash";
    private static final String FILE_NAME_SUFFIX = ".trace";

    public static void v(String tag, String msg) {
        if (level <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (level <= DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (level <= INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (level <= WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (level <= ERROR) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (level <= ERROR) {
            Log.e(tag, msg, tr);
        }
    }

    public static void printStackTrace(Context context, Throwable ex, boolean ignoreLevel) {
        if (level > ERROR && !ignoreLevel) return;

        StringBuilder sb = getStackTraceString(ex);

        Log.e(context.getPackageName(), sb.toString());
    }

    public static void printStackTrace(Context context, Throwable ex) {
        printStackTrace(context, ex, false);
    }

    @NonNull
    private static StringBuilder getStackTraceString(Throwable ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append(ex.toString()).append("\n");
        // Print our stack trace
        for (StackTraceElement traceElement : stackTrace) {
            sb.append("\tat ").append(traceElement.toString()).append("\n");
        }

        // Print cause, if any
        Throwable ourCause = ex.getCause();
        if (ourCause != null) {
            sb.append("Caused by:").append(ex.getLocalizedMessage().split(":")[1]);
            sb.append(" : ").append(ourCause.getLocalizedMessage()).append("\n");
            StackTraceElement[] ourCauseStackTrace = ourCause.getStackTrace();
            for (StackTraceElement traceElement : ourCauseStackTrace) {
                sb.append("\tat ").append(traceElement.toString()).append("\n");
            }
        }
        return sb;
    }

    public static void printStackTrace(Throwable ex) {
        printStackTrace(MyApplication.getContext(), ex);
    }

    /**
     * 将异常信息写入SD卡
     *
     * @param ex
     */
    public static void dumpException2SDCard(Throwable ex) {
        //如果SD卡不存在或无法使用，则无法把异常信息写入SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.w("dumpException2SDCard", "sdcard unmounted,skip dump exception");
            return;
        }
        File dir = new File(PATH);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return;
            }
        }
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(current));
        File file = new File(PATH, FILE_NAME + "_" + time + FILE_NAME_SUFFIX);

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            String app_name = MyApplication.getContext().getString(R.string.app_name);
            pw.println(">>>>" + app_name + " app crash log file >>>>\n");
            pw.println(time);
            dumpDeviceInfo(pw);
            pw.println("\nexception detail:");
            pw.println("------------------------------------------");
            //ex.printStackTrace(pw);
            pw.println(getStackTraceString(ex).toString());
            pw.close();
        } catch (Exception e) {
            e("dumpException2SDCard", "dump crash info failed");
        }
    }

    /**
     * 将设备信息写入PrintWriter
     *
     * @param pw
     * @throws PackageManager.NameNotFoundException
     */
    public static void dumpDeviceInfo(Context context, PrintWriter pw) throws PackageManager
            .NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager
                .GET_ACTIVITIES);
        pw.println("\ndevice info:");
        pw.println("------------------------------------------");
        // 应用版本信息
        pw.println("VersionName:" + packageInfo.versionName);
        pw.println("VersionCode:" + packageInfo.versionCode);

        //android版本号,sdk
        pw.println("Os Version:" + Build.VERSION.RELEASE);
        pw.println("Os SDK:" + Build.VERSION.SDK_INT);

        //手机制造商
        pw.println("Vendor: " + Build.MANUFACTURER);

        //手机型号
        pw.println("Model: " + Build.MODEL);

        //cpu架构
        pw.println("CPU ABI: " + Build.CPU_ABI);
    }

    public static void dumpDeviceInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        dumpDeviceInfo(MyApplication.getContext(), pw);
    }
}
