package com.ckt.testauxiliarytool.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.ckt.testauxiliarytool.autosleepwake.WakeUpActivity;
import com.ckt.testauxiliarytool.autosleepwake.receiver.AdminReceiver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 测试所用工具类
 */
public final class TestUtil {
    public static final String ACTION_REBOOT = "android.intent.action.REBOOT";
    public static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    public static final String TAG = TestUtil.class.getSimpleName();

    private static DevicePolicyManager sDevicePolicyManager;
    private static ComponentName sComponentName;
    private static PowerManager sPowerManager;
    private static PowerManager.WakeLock sWakeLock;
    private static final DateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private TestUtil() {
        throw new UnsupportedOperationException("you can't instantiate TestUtil");
    }

    private static DevicePolicyManager getDevicePolicyManager(Context context) {
        if (sDevicePolicyManager == null) {
            sDevicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        return sDevicePolicyManager;
    }

    private static ComponentName getComponentName(Context context) {
        if (sComponentName == null) {
            sComponentName = new ComponentName(context, AdminReceiver.class);
        }
        return sComponentName;
    }

    private static PowerManager getPowerManager(Context context) {
        if (sPowerManager == null) {
            sPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        }
        return sPowerManager;
    }

    private static PowerManager.WakeLock getWakeLock(Context context) {
        if (sWakeLock == null) {
            sWakeLock = getPowerManager(context).newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.FULL_WAKE_LOCK, "TestUtilWakeLock");
        }
        return sWakeLock;
    }

    /**
     * 执行锁屏操作，相当于按下电源键
     *
     * @param context 上下文，用于获取 DevicePolicyManager
     */
    public static void lock(Context context, String logPath, int currentCount) {
        DevicePolicyManager devicePolicyManager = getDevicePolicyManager(context);
        // 有必要再次确认是否激活，防止用户点击测试按钮时激活了，
        // 但在测试运行过程中，又跑去设备管理器取消了激活，导致应用崩溃
        if (devicePolicyManager.isAdminActive(getComponentName(context))) {
            devicePolicyManager.lockNow();
            if (FileUtils.isFileExists(logPath)) {
                FileUtils.writeFileFromString(logPath, "第" + currentCount + "次休眠：" + millis2String(System.currentTimeMillis()) + "\n", true);
            }
            LogUtil.d(TAG,"休眠：" + millis2String(System.currentTimeMillis()));
        }
    }

    /**
     * 判断是否在设备管理器中激活应用
     *
     * @param context 上下文
     * @return true表示激活，false表示未激活
     */
    public static boolean isAdminActive(Context context) {
        return getDevicePolicyManager(context).isAdminActive(getComponentName(context));
    }

    /**
     * 利用打开新的Activity来唤醒屏幕，已废弃，具体原因请见WakeUpActivity，
     * 请使用wakeUp()方法替代
     *
     * @param context 上下文
     */
    @Deprecated
    public static void wakeUpByActivity(Context context) {
        Intent intent = new Intent(context, WakeUpActivity.class);
        // 因为需要在服务中启动InfoActivity，所以需要添加如下标志
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 显示对话框
     *
     * @param dialog          对话框对象
     * @param fragmentManager Fragment管理者
     * @param tag             Fragment的tag
     */
    public static void showDialog(DialogFragment dialog, FragmentManager fragmentManager, String tag) {
        dialog.setCancelable(false);
        dialog.show(fragmentManager, tag);
    }

    /**
     * 通过PowerManager.WakeLock唤醒锁来唤醒屏幕
     *
     * @param context 上下文
     */
    public static void wakeUp(Context context, String logPath, int currentCount) {
        getWakeLock(context).acquire();
        getWakeLock(context).release();
        if (FileUtils.isFileExists(logPath)) {
            FileUtils.writeFileFromString(logPath, "第" + currentCount + "次唤醒：" + millis2String(System.currentTimeMillis()) + "\n", true);
        }
        LogUtil.d(TAG,"唤醒：" + millis2String(System.currentTimeMillis()));
    }

    /**
     * 使用默认格式将时间戳转化为时间字符串
     *
     * @param millis 毫秒时间戳
     * @return 格式化的时间字符串，如 2017-09-15 18:37:13
     */
    public static String millis2String(final long millis) {
        return millis2String(millis, DEFAULT_FORMAT);
    }

    /**
     * 将时间戳格式化为时间字符串
     *
     * @param millis 毫秒时间戳
     * @param format 转换格式
     * @return 格式化的时间字符串
     */
    private static String millis2String(final long millis, final DateFormat format) {
        return format.format(new Date(millis));
    }

    public static void rebootDevice(Context context) {
        DevicePolicyManager policyManager = getDevicePolicyManager(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            policyManager.reboot(getComponentName(context));
        }
    }

    public static void rebootDeviceWithBroadcast(Context context) {
        Intent intent = new Intent(ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
        // context.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

}
